package com.myplants

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.myplants.adapter.CommunityPlantAdapter
import com.myplants.model.Plant

class CommunityGardenFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CommunityPlantAdapter
    private val db = FirebaseFirestore.getInstance()
    private var selectedUserId: String? = null // This should be passed from CommunityFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_community_garden, container, false)
        recyclerView = view.findViewById(R.id.rvCommunityGardenPlants)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = CommunityPlantAdapter(listOf())
        recyclerView.adapter = adapter


        // Retrieve the passed arguments
        selectedUserId = arguments?.getString("userId")

        db.collection("users").document(selectedUserId.toString()).get()
            .addOnSuccessListener { document ->
                val username = document.getString("username")
                val title = resources.getString(R.string.garden_title, username)
                view.findViewById<TextView>(R.id.tv_community_garden_title).text = title
            }

        selectedUserId?.let { userId ->
            fetchPlantsForUser(userId)
        }

        return view
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
                        imageUrl = doc.getString("imageUrl") ?: "" // Use default or placeholder if null
                    )
                }
                adapter.updatePlants(plants)
            }
            .addOnFailureListener { exception ->
                Log.d("CommunityGardenFragment", "Error getting plants: ", exception)
            }
    }
}
