package com.myplants.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.myplants.R
import com.myplants.model.Garden

class GardenAdapter(private var gardens: List<Garden>, private val listener: GardenActionListener) : RecyclerView.Adapter<GardenAdapter.GardenViewHolder>() {

    interface GardenActionListener {
        fun onViewGarden(userId: String)
    }

    class GardenViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ownerNameTextView: TextView = view.findViewById(R.id.ownerName)
        val viewGardenButton: Button = view.findViewById(R.id.btnViewGarden)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GardenViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_garden, parent, false)
        return GardenViewHolder(view)
    }

    override fun onBindViewHolder(holder: GardenViewHolder, position: Int) {
        val garden = gardens[position]
        holder.ownerNameTextView.text = garden.ownerName
        holder.viewGardenButton.setOnClickListener {
            listener.onViewGarden(garden.userId)
        }
    }

    override fun getItemCount() = gardens.size

    fun updateGardens(newGardens: List<Garden>) {
        gardens = newGardens
        notifyDataSetChanged()
    }
}
