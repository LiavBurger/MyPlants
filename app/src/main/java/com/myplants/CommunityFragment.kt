package com.myplants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.myplants.adapter.GardenAdapter
import com.myplants.model.Garden


class CommunityFragment : Fragment(), GardenAdapter.GardenActionListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GardenAdapter

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

    private fun fetchCommunityGardens() {
        // Mock data for demonstration; replace with actual Firestore fetching logic
        val mockGardens = listOf(
            Garden(userId = "user1", ownerName = "Alice"),
            Garden(userId = "user2", ownerName = "Bob"),
            Garden(userId = "user1", ownerName = "Alice"),
            Garden(userId = "user2", ownerName = "Bob"),
            Garden(userId = "user1", ownerName = "Alice"),
            Garden(userId = "user2", ownerName = "Bob"),
            Garden(userId = "user1", ownerName = "Alice"),
            Garden(userId = "user2", ownerName = "Bob"),
            Garden(userId = "user1", ownerName = "Alice"),
            Garden(userId = "user2", ownerName = "Bob"),
            Garden(userId = "user1", ownerName = "Alice"),
            Garden(userId = "user2", ownerName = "Bob"),
            // Add more mock gardens as needed
        )
        adapter.updateGardens(mockGardens)
    }

    override fun onViewGarden(userId: String) {
        // Implement navigation to view a selected garden
        // This will require passing the userId to the GardenFragment or a similar fragment for displaying other users' gardens
    }
}
