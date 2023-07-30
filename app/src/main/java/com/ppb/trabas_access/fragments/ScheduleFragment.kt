package com.ppb.trabas_access.fragments

import ScheduleAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ppb.trabas_access.R

class ScheduleFragment : Fragment() {

    private val scheduleItems = listOf(
        "Event 1",
        "Event 2",
        "Event 3",
        "Event 4",
        "Event 5"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_schedule, container, false)

        // Find the RecyclerView from the XML layout
        val recyclerView: RecyclerView = rootView.findViewById(R.id.recycleview)

        // Set up the RecyclerView with a LinearLayoutManager
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Create an adapter and attach it to the RecyclerView
        val adapter = ScheduleAdapter(scheduleItems)
        recyclerView.adapter = adapter

        return rootView
    }
}