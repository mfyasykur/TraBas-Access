package com.ppb.trabas_access.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ppb.trabas_access.model.dao.Schedule

class ScheduleRepository {

    private val scheduleRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("schedule")

    fun getScheduleData(callback: (List<Schedule>) -> Unit) {
        val scheduleList = mutableListOf<Schedule>()

        scheduleRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val schedule = dataSnapshot.getValue(Schedule::class.java)
                    schedule?.let {
                        scheduleList.add(it)
                        Log.d("ScheduleRepository", "Schedule: $it")
                    }
                }
                callback(scheduleList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ScheduleRepository", "Data retrieval canceled: ${error.message}")
            }
        })
    }

}