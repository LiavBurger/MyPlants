package com.myplants.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.myplants.R
import com.myplants.model.Plant
import com.squareup.picasso.Picasso

class CommunityPlantAdapter(private var plants: List<Plant>) : RecyclerView.Adapter<CommunityPlantAdapter.CommunityPlantViewHolder>() {

    class CommunityPlantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val plantNameTextView: TextView = view.findViewById(R.id.tv_plant_name)
        val plantTypeTextView: TextView = view.findViewById(R.id.tv_plant_type)
        val plantImageView: ImageView = view.findViewById(R.id.plantImage)
        // You can also add an ImageView for plant image when you have plant images
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityPlantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_plant_community, parent, false)
        return CommunityPlantViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommunityPlantViewHolder, position: Int) {
        val plant = plants[position]
        holder.plantNameTextView.text = plant.name
        holder.plantTypeTextView.text = plant.type
        Picasso.get().load(plant.imageUrl).into(holder.plantImageView)
        // Set the plant image if available
    }

    override fun getItemCount() = plants.size

    fun updatePlants(newPlants: List<Plant>) {
        plants = newPlants
        notifyDataSetChanged()
    }
}
