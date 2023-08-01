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
import com.ppb.trabas_access.databinding.FragmentScheduleBinding
import com.ppb.trabas_access.repository.ScheduleRepository

class ScheduleFragment : Fragment() {

    private lateinit var binding: FragmentScheduleBinding
    private lateinit var scheduleAdapter: ScheduleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScheduleBinding.inflate(inflater, container, false)
        Log.d("ScheduleFragment", "onCreateView() called")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = binding.recycleview

        scheduleAdapter = ScheduleAdapter()
        recyclerView.adapter = scheduleAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize the ScheduleRepository
        val scheduleRepository = ScheduleRepository()

        // Get the schedule data from the repository
        scheduleRepository.getScheduleData { scheduleList ->
            scheduleAdapter.submitList(scheduleList)
        }
    }
}
