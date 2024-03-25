package com.myplants.viewmodel

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.myplants.view.adapter.GardenAdapter
import com.myplants.database.AppDatabase
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.myplants.R
import com.myplants.model.Garden
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class CommunityFragment : Fragment(), GardenAdapter.GardenActionListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GardenAdapter
    private lateinit var database: AppDatabase
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_community, container, false)
        recyclerView = view.findViewById(R.id.rvCommunityGardens)
        adapter = GardenAdapter(listOf(), this)
        recyclerView.adapter = adapter
        database = AppDatabase.getDatabase(requireContext())

        loadGardensFromCache()
        fetchCommunityGardens()

        return view
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // Load from cache first
//        loadGardensFromCache()
//
//        // Then, asynchronously fetch from Firestore and update cache
//        fetchCommunityGardens()
//    }

    private fun loadGardensFromCache() {
        print("Loading gardens from cache")
        lifecycleScope.launch(Dispatchers.IO) {
            val cachedGardens = database.gardenDao().getAll()
            withContext(Dispatchers.Main) {
                adapter.updateGardens(cachedGardens)
            }
        }
    }

    private fun fetchCommunityGardens() {
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                val gardens = mutableListOf<Garden>()
                for (doc in documents) {
                    val userId = doc.id
                    val ownerName = doc.getString("username") ?: "Unknown"
                    val ownerImageUrl = doc.getString("imageUrl") ?: ""

                    gardens.add(Garden(userId, ownerName, ownerImageUrl))
                }

                // Update the local cache with the fetched gardens
                lifecycleScope.launch {
                    // Clear existing cache and insert new data
                    database.gardenDao().deleteAll()
                    database.gardenDao().insertAll(gardens)

                    // Now update the UI with the new list from cache
                    adapter.updateGardens(database.gardenDao().getAll())
                }
            }
            .addOnFailureListener { exception ->
                Log.d("CommunityFragment", "Error getting community gardens: ", exception)

                // If fetch fails, attempt to load from cache anyway
                lifecycleScope.launch {
                    adapter.updateGardens(database.gardenDao().getAll())
                }
            }
    }



    override fun onViewGarden(userId: String) {
        val action = CommunityFragmentDirections.actionCommunityFragmentToCommunityGardenFragment(userId)
        findNavController().navigate(action)
    }

}
