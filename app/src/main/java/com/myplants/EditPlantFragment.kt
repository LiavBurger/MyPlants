package com.myplants

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.UUID

class EditPlantFragment : Fragment() {

    private lateinit var plantNameEditText: EditText
    private lateinit var plantTypeEditText: EditText
    private lateinit var plantImageView: ImageView
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                Picasso.get().load(selectedImageUri).into(plantImageView)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_plant, container, false)

        plantNameEditText = view.findViewById(R.id.etPlantName)
        plantTypeEditText = view.findViewById(R.id.etPlantType)
        plantImageView = view.findViewById(R.id.plantImage)

        val plantId = arguments?.getString("plantId") ?: ""
        val imageUrl = arguments?.getString("plantImage")
        Picasso.get().load(imageUrl).into(plantImageView)
        plantNameEditText.setText(arguments?.getString("plantName"))
        plantTypeEditText.setText(arguments?.getString("plantType"))

        view.findViewById<Button>(R.id.btnSaveChanges).setOnClickListener {
            savePlantChanges(plantId)
        }

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            // Navigate back to the previous fragment
            findNavController().popBackStack()
        }

        view.findViewById<FloatingActionButton>(R.id.fabtnCamera).setOnClickListener {
            openGalleryForImage()
        }

        return view
    }

    private fun savePlantChanges(plantId: String) {
        val name = plantNameEditText.text.toString().trim()
        val type = plantTypeEditText.text.toString().trim()

        if (name.isEmpty() || type.isEmpty()) {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri != null) {
            uploadImageAndUpdatePlant(plantId, name, type)
        } else {
            updatePlant(plantId, name, type, null)
        }
    }

    private fun uploadImageAndUpdatePlant(plantId: String, name: String, type: String) {
        val storageRef = FirebaseStorage.getInstance().reference.child("plantImages/${UUID.randomUUID()}")
        selectedImageUri?.let { imageUri ->
            storageRef.putFile(imageUri).addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()
                    updatePlant(plantId, name, type, imageUrl)
                }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Error uploading image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePlant(plantId: String, name: String, type: String, newImageUrl: String?) {
        val updatedPlant = hashMapOf(
            "name" to name,
            "type" to type
        ).apply { newImageUrl?.let { put("imageUrl", it) } }

        // delete the old image if a new one was uploaded
        if (newImageUrl != null) {
            val oldImageUrl = arguments?.getString("plantImage") ?: ""
            deleteOldImage(oldImageUrl)
        }

        // update the plant in the database
        db.collection("users").document(userId)
            .collection("plants").document(plantId)
            .update(updatedPlant as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(context, "Plant updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error updating plant", Toast.LENGTH_SHORT).show()
            }

    }

    private fun deleteOldImage(imageUrl: String) {
        val oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        oldImageRef.delete().addOnFailureListener { e ->
            Log.e("EditPlantFragment", "Error deleting old image: ${e.localizedMessage}")
        }
    }


    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        }
        pickImageLauncher.launch(intent)
    }
}
