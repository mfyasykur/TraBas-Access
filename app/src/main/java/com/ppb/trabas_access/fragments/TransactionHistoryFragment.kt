package com.ppb.trabas_access.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ppb.trabas_access.adapter.TransactionHistoryAdapter
import com.ppb.trabas_access.databinding.FragmentTransactionHistoryBinding
import com.ppb.trabas_access.model.dao.TransactionHistory

class TransactionHistoryFragment : Fragment() {

    private lateinit var binding: FragmentTransactionHistoryBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionHistoryAdapter: TransactionHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTransactionHistoryBinding.inflate(inflater, container, false)

        // Set up RecyclerView and Adapter
        recyclerView = binding.rvTransactionHistory
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        transactionHistoryAdapter = TransactionHistoryAdapter()
        recyclerView.adapter = transactionHistoryAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch the transaction history data from Firebase
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { _ ->
            val transactionHistoryRef = FirebaseDatabase.getInstance().reference
                .child("transactionHistory")
                .orderByChild("timeStamp")

            transactionHistoryRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val transactionList = mutableListOf<TransactionHistory>()
                    for (data in snapshot.children) {
                        val transaction = data.getValue(TransactionHistory::class.java)
                        transaction?.let {
                            transactionList.add(it)
                        }
                    }

                    // Update the data in the adapter
                    transactionHistoryAdapter.setData(transactionList)
                }

                override fun onCancelled(error: DatabaseError) {
                    showToast("Failed to fetch transaction history")
                }
            })
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}