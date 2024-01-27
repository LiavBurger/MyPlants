package com.myplants

import PlantAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.myplants.model.Plant

class GardenFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlantAdapter
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_garden, container, false)
        recyclerView = view.findViewById(R.id.rvViewGarden)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = PlantAdapter(listOf())
        recyclerView.adapter = adapter

        // Add button
        val addButton: Button = view.findViewById(R.id.btnAddNewPlant)
        addButton.setOnClickListener {
            findNavController().navigate(R.id.action_gardenFragment_to_addPlantFragment)
        }


        // Use mock data for testing
        val mockPlants = generateMockPlants()
        adapter.updatePlants(mockPlants)

        // real data
        // fetchPlantsForUser(userId)
        return view
    }

    private fun fetchPlantsForUser(userId: String) {
        db.collection("users").document(userId)
            .collection("gardens").document("main_garden")
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

    private fun generateMockPlants(): List<Plant> {
        return listOf(
            Plant(id = "1", name = "Rose", type = "Flower", imageUrl = "Default Image URL"),
            Plant(id = "2", name = "Tulip", type = "Flower", imageUrl = "Default Image URL"),
            Plant(id = "3", name = "Fern", type = "Foliage", imageUrl = "Default Image URL"),
            Plant(id = "4", name = "Cactus", type = "Succulent", imageUrl = "Default Image URL"),
            Plant(id = "5", name = "Cactus2", type = "Succulent", imageUrl = "Default Image URL"),
            Plant(id = "6", name = "Cactus3", type = "Succulent", imageUrl = "Default Image URL"),
            Plant(id = "7", name = "Cactus4", type = "Succulent", imageUrl = "Default Image URL"),
            Plant(id = "8", name = "Cactus5", type = "Succulent", imageUrl = "Default Image URL")
            // Add more mock plants as needed
        )
    }

}
