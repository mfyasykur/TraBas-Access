import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ppb.trabas_access.R
import com.ppb.trabas_access.model.dao.Schedule

class ScheduleAdapter : ListAdapter<Schedule, ScheduleAdapter.ViewHolder>(ScheduleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scheduleItem = getItem(position)
        holder.bind(scheduleItem)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewHalte: TextView = itemView.findViewById(R.id.tv_halte_name)
        private val textViewTime: TextView = itemView.findViewById(R.id.tv_time)

        fun bind(scheduleItem: Schedule) {
            textViewHalte.text = scheduleItem.halte
            textViewTime.text = scheduleItem.time
        }
    }

    class ScheduleDiffCallback : DiffUtil.ItemCallback<Schedule>() {
        override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem.halte == newItem.halte
        }

        override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem == newItem
        }
    }

}
