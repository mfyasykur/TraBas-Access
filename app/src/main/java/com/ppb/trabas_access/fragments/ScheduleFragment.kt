package com.ppb.trabas_access.fragments

import ScheduleAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ppb.trabas_access.databinding.FragmentScheduleBinding
import com.ppb.trabas_access.model.dao.Schedule

class ScheduleFragment : Fragment() {

    private lateinit var binding: FragmentScheduleBinding
    private lateinit var scheduleAdapter: ScheduleAdapter
    private lateinit var scheduleRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScheduleBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = binding.recycleview

        scheduleRef = FirebaseDatabase.getInstance().reference.child("schedule")

        scheduleAdapter = ScheduleAdapter()
        recyclerView.adapter = scheduleAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        getScheduleData { scheduleList ->
            scheduleAdapter.submitList(scheduleList)
        }
    }

    private fun getScheduleData(callback: (List<Schedule>) -> Unit) {
        val scheduleList = mutableListOf<Schedule>()

        scheduleRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val schedule = dataSnapshot.getValue(Schedule::class.java)
                    schedule?.let {
                        scheduleList.add(it)
                        Log.d("ScheduleFragment", "Schedule: $it")
                    }
                }
                callback(scheduleList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ScheduleFragment", "Data retrieval canceled: ${error.message}")
            }
        })
    }
}
