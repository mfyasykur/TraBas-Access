import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ppb.trabas_access.R

class ScheduleAdapter(private val scheduleItems: List<String>) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scheduleItem = scheduleItems[position]
        holder.bind(scheduleItem)
    }

    override fun getItemCount(): Int {
        return scheduleItems.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.nama_tujuan)

        fun bind(scheduleItem: String) {
            textView.text = scheduleItem
        }
    }
}
