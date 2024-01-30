package com.myplants

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.fragment.findNavController
class EditPlantFragment : Fragment() {

    private lateinit var plantNameEditText: EditText
    private lateinit var plantTypeEditText: EditText
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_plant, container, false)

        plantNameEditText = view.findViewById(R.id.etPlantName)
        plantTypeEditText = view.findViewById(R.id.etPlantType)

        val plantId = arguments?.getString("plantId") ?: ""
        plantNameEditText.setText(arguments?.getString("plantName"))
        plantTypeEditText.setText(arguments?.getString("plantType"))

        view.findViewById<Button>(R.id.btnSaveChanges).setOnClickListener {
            savePlantChanges(plantId)
        }

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            // Navigate back to the previous fragment
            findNavController().popBackStack()
        }

        return view
    }

    private fun savePlantChanges(plantId: String) {
        val name = plantNameEditText.text.toString().trim()
        val type = plantTypeEditText.text.toString().trim()

        if (name.isNotEmpty() && type.isNotEmpty()) {
            val updatedPlant = hashMapOf(
                "name" to name,
                "type" to type
                // Add image URL if you're handling images
            )

            userId.let { uid ->
                db.collection("users").document(uid)
                    .collection("plants").document(plantId)
                    .update(updatedPlant as Map<String, Any>)
                    .addOnSuccessListener {
                        findNavController().popBackStack()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error updating plant", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }
}
