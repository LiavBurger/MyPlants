package com.myplants

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class AddPlantFragment : Fragment() {

    private lateinit var plantNameEditText: EditText
    private lateinit var plantTypeEditText: EditText
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_plant, container, false)
        plantNameEditText = view.findViewById(R.id.etPlantName)
        plantTypeEditText = view.findViewById(R.id.etPlantType)

        view.findViewById<Button>(R.id.btnAddPlant).setOnClickListener {
            addPlantToUserGarden()
        }

        val cancelButton: Button = view.findViewById(R.id.btnCancel)
        cancelButton.setOnClickListener {
            // Navigate back to the previous fragment
            findNavController().popBackStack()
        }

        return view
    }

    private fun addPlantToUserGarden() {
        val name = plantNameEditText.text.toString().trim()
        val type = plantTypeEditText.text.toString().trim()

        if (name.isNotEmpty() && type.isNotEmpty()) {
            val plant = hashMapOf(
                "name" to name,
                "type" to type,
                "imageUrl" to "Default Image URL" // Placeholder, replace with actual image URL after implementing image upload
            )

            userId?.let { uid ->
                db.collection("users").document(uid)
                    .collection("plants")
                    .add(plant)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Plant added successfully!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack() // Navigate back to the GardenFragment
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error adding plant: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }
}
