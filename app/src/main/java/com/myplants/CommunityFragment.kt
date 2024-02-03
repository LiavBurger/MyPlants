package com.myplants

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.myplants.adapter.GardenAdapter
import com.myplants.model.Garden
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore

class CommunityFragment : Fragment(), GardenAdapter.GardenActionListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GardenAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_community, container, false)
        recyclerView = view.findViewById(R.id.rvCommunityGardens)
        adapter = GardenAdapter(listOf(), this)
        recyclerView.adapter = adapter

        fetchCommunityGardens()

        return view
    }

//    private fun fetchCommunityGardens() {
//        // Mock data for demonstration; replace with actual Firestore fetching logic
//        val mockGardens = listOf(
//            Garden(userId = "user1", ownerName = "Alice"),
//            Garden(userId = "user2", ownerName = "Bob"),
//            Garden(userId = "user1", ownerName = "Alice"),
//            Garden(userId = "user2", ownerName = "Bob"),
//            Garden(userId = "user1", ownerName = "Alice"),
//            Garden(userId = "user2", ownerName = "Bob"),
//            Garden(userId = "user1", ownerName = "Alice"),
//            Garden(userId = "user2", ownerName = "Bob"),
//            Garden(userId = "user1", ownerName = "Alice"),
//            Garden(userId = "user2", ownerName = "Bob"),
//            Garden(userId = "user1", ownerName = "Alice"),
//            Garden(userId = "user2", ownerName = "Bob"),
//            // Add more mock gardens as needed
//        )
//        adapter.updateGardens(mockGardens)
//    }

    private fun fetchCommunityGardens() {
        val gardens = mutableListOf<Garden>()

        // Fetch all users from Firestore
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val userId = doc.id
                    val ownerName = doc.getString("username") ?: "Unknown"
                    val ownerImageUrl = doc.getString("imageUrl") ?: ""

                    // Create a Garden object and add it to the list
                    gardens.add(Garden(userId, ownerName, ownerImageUrl))
                }

                // Update the RecyclerView adapter with the fetched gardens
                adapter.updateGardens(gardens)
            }
            .addOnFailureListener { exception ->
                Log.d("CommunityFragment", "Error getting community gardens: ", exception)
            }
    }


    override fun onViewGarden(userId: String) {
        val action = CommunityFragmentDirections.actionCommunityFragmentToCommunityGardenFragment(userId)
        findNavController().navigate(action)
    }

}
