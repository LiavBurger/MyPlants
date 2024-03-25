package com.myplants.view.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.myplants.R
import com.squareup.picasso.Picasso
import java.util.UUID


class AddPlantFragment : Fragment() {

    private lateinit var plantNameEditText: EditText
    private lateinit var plantTypeEditText: EditText
    private lateinit var plantImageView: ImageView
    private var selectedImageUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Picasso.get().load(selectedImageUri).into(plantImageView)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_add_plant, container, false)

        plantNameEditText = view.findViewById(R.id.etPlantName)
        plantTypeEditText = view.findViewById(R.id.etPlantType)
        plantImageView = view.findViewById(R.id.plantImage)

        view.findViewById<FloatingActionButton>(R.id.fabtnCamera).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        view.findViewById<Button>(R.id.btnAddPlant).setOnClickListener {
            addPlantToUserGarden()
        }

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            // Navigate back to the previous fragment
            findNavController().popBackStack()
        }

        return view
    }

    private fun addPlantToUserGarden() {
        val name = plantNameEditText.text.toString().trim()
        val type = plantTypeEditText.text.toString().trim()

        if (name.isNotEmpty() && type.isNotEmpty() && selectedImageUri != null) {
            uploadImageAndAddPlant(selectedImageUri!!, name, type)
        } else {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageAndAddPlant(imageUri: Uri, name: String, type: String) {
        val storageRef = FirebaseStorage.getInstance().reference.child("plantImages/${UUID.randomUUID()}")

        storageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    addPlantToDatabase(name, type, imageUrl)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error uploading image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun addPlantToDatabase(name: String, type: String, imageUrl: String) {
        val plant = hashMapOf(
            "name" to name,
            "type" to type,
            "imageUrl" to imageUrl
        )

        userId?.let { uid ->
            db.collection("users").document(uid).collection("plants").add(plant).addOnSuccessListener {
                Toast.makeText(context, "Plant added successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Error adding plant: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
