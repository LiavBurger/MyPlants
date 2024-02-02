package com.myplants

import PlantAdapter
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.myplants.model.Plant

class GardenFragment : Fragment(), PlantAdapter.PlantActionListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlantAdapter
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_garden, container, false)
        recyclerView = view.findViewById(R.id.rvViewGarden)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = PlantAdapter(listOf(), this)
        recyclerView.adapter = adapter

        // Add button
        val addButton: Button = view.findViewById(R.id.btnAddNewPlant)
        addButton.setOnClickListener {
            findNavController().navigate(R.id.action_gardenFragment_to_addPlantFragment)
        }

        // real data
        fetchPlantsForUser(userId)
        return view
    }

    override fun onDeletePlant(plantId: String) {
        showDeleteConfirmationDialog(plantId)
    }

    override fun onEditPlant(plant: Plant) {
        val action = GardenFragmentDirections.actionGardenFragmentToEditPlantFragment(plant.id, plant.name, plant.type, plant.imageUrl)
        findNavController().navigate(action)
    }


    private fun showDeleteConfirmationDialog(plantId: String) {
        activity?.let {
            AlertDialog.Builder(it)
                .setTitle("Delete Plant")
                .setMessage("Are you sure you want to delete this plant?")
                .setPositiveButton("Yes") { dialog, which ->
                    deletePlant(plantId)
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun deletePlant(plantId: String) {
        userId.let { uid ->
            val plantRef = db.collection("users").document(uid).collection("plants").document(plantId)
            deletePlantImage(plantRef) {
                deletePlantDocument(plantRef)
            }
        }
    }

    private fun deletePlantImage(plantRef: DocumentReference, onSuccess: () -> Unit) {
        plantRef.get()
            .addOnSuccessListener { document ->
                val imageUrl = document.getString("imageUrl")
                val imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl!!)
                imageRef.delete()
                    .addOnSuccessListener {
                        onSuccess()
                    }
            }
    }

    private fun deletePlantDocument(plantRef: DocumentReference) {
        plantRef.delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Plant deleted", Toast.LENGTH_SHORT).show()
                // refresh the UI
                fetchPlantsForUser(userId)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error deleting plant", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchPlantsForUser(userId: String) {
        db.collection("users").document(userId)
            .collection("plants")
            .get()
            .addOnSuccessListener { documents ->
                val plants = documents.map { doc ->
                    Plant(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        type = doc.getString("type") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: ""
                    )
                }
                adapter.updatePlants(plants)
            }
            .addOnFailureListener { exception ->
                Log.d("GardenFragment", "Error getting plants: ", exception)
            }
    }
}
