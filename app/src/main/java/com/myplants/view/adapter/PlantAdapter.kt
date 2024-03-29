import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.myplants.R
import com.myplants.model.Plant
import com.squareup.picasso.Picasso

class PlantAdapter(private var plants: List<Plant>, private val listener: PlantActionListener) : RecyclerView.Adapter<PlantAdapter.PlantViewHolder>() {

    class PlantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val plantImage: ImageView = view.findViewById(R.id.ownerImage)
        val plantName: TextView = view.findViewById(R.id.tv_plant_name)
        val plantType: TextView = view.findViewById(R.id.tv_plant_type)

        val editButton: Button = view.findViewById(R.id.btnEditPlant)
        val deleteButton: Button = view.findViewById(R.id.btnDeletePlant)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_plant, parent, false)
        return PlantViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        val plant = plants[position]

        holder.plantName.text = plant.name
        holder.plantType.text = plant.type
        Picasso.get().load(plant.imageUrl).into(holder.plantImage)

        // Add click listeners
        holder.editButton.setOnClickListener {
            listener.onEditPlant(plant)
        }
        holder.deleteButton.setOnClickListener {
            listener.onDeletePlant(plant.id)
        }
    }

    override fun getItemCount() = plants.size

    fun updatePlants(newPlants: List<Plant>) {
        plants = newPlants
        notifyDataSetChanged()
    }

    interface PlantActionListener {
        fun onDeletePlant(plantId: String)
        fun onEditPlant(plant: Plant)
    }

}
