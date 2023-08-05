package com.ppb.trabas_access.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ppb.trabas_access.R
import com.ppb.trabas_access.databinding.FragmentTransactionHistoryBinding
import com.ppb.trabas_access.model.dao.TransactionHistory
import java.text.NumberFormat
import java.util.Locale

class TransactionHistoryAdapter: RecyclerView.Adapter<TransactionHistoryAdapter.ViewHolder>() {

    private val transactionList = mutableListOf<TransactionHistory>()

    fun setData(data: List<TransactionHistory>) {
        transactionList.clear()
        transactionList.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactionList[position]
        holder.bind(transaction)
    }

    override fun getItemCount(): Int = transactionList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTransactionId: TextView = itemView.findViewById(R.id.tv_transaction_id)
        private val tvAmount: TextView = itemView.findViewById(R.id.tv_amount)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        private val tvTimeStamp: TextView = itemView.findViewById(R.id.tv_timeStamp)
        private val ifDataEmpty: TextView = itemView.findViewById(R.id.tv_if_data_empty)

        fun bind(transaction: TransactionHistory) {

            val formattedBalance = transaction.ticketPrice?.let { formatCurrency(it) }

            if (tvTransactionId.text.isNotEmpty()) {
                Log.d("TransactionHistoryAdapter", "IF dijalankan")
                ifDataEmpty.visibility = View.GONE
                tvTransactionId.text = "ID Transaksi: ${transaction.transactionId}"
                tvAmount.text = "Nominal: Rp. $formattedBalance"
                tvStatus.text = transaction.status
                tvTimeStamp.text = transaction.timeStamp.toString()
            } else {
                Log.d("TransactionHistoryAdapter", "ELSE dijalankan")
                ifDataEmpty.visibility = View.VISIBLE
                tvTransactionId.visibility = View.GONE
                tvAmount.visibility = View.GONE
                tvStatus.visibility = View.GONE
                tvTimeStamp.visibility = View.GONE
            }
        }
    }

    private fun formatCurrency(amount: Long): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
        numberFormat.maximumFractionDigits = 0
        return numberFormat.format(amount)
    }
}