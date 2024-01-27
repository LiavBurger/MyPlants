import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.myplants.R
import com.myplants.model.Plant

class PlantAdapter(private var plants: List<Plant>) : RecyclerView.Adapter<PlantAdapter.PlantViewHolder>() {

    class PlantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val plantImage: ImageView = view.findViewById(R.id.imgPlantImage)
        val plantName: TextView = view.findViewById(R.id.tv_user_input_name)
        val plantType: TextView = view.findViewById(R.id.tv_user_input_type)
        val viewButton: Button = view.findViewById(R.id.btnViewPlant)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_plant, parent, false)
        return PlantViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        val plant = plants[position]
        // Set plant image, name, and type here
        // For now, using a default image. Replace with actual image loading logic.
        holder.plantName.text = plant.name
        holder.plantType.text = plant.type
        // Add click listener for viewButton if needed
    }

    override fun getItemCount() = plants.size

    fun updatePlants(newPlants: List<Plant>) {
        plants = newPlants
        notifyDataSetChanged()
    }
}
