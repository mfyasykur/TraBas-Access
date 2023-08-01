package com.ppb.trabas_access.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ppb.trabas_access.databinding.FragmentFindDestinationBinding
import com.ppb.trabas_access.model.dao.Destination
import com.ppb.trabas_access.model.dao.Koridor

class FindDestinationFragment : Fragment() {

    private lateinit var binding: FragmentFindDestinationBinding
    private lateinit var destinationRef: DatabaseReference
    private lateinit var koridorRef: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val koridorList: MutableList<Koridor> = mutableListOf()
    private val locationPermissionCode = 123
    private var currentLatitude = 0.0
    private var currentLongitude = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFindDestinationBinding.inflate(inflater, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        binding.gridResultHalte.visibility = View.GONE
        binding.gridResultHalte2.visibility = View.GONE
        binding.tvHasil.visibility = View.GONE

        // Get the destination name from the arguments
        val destinationName = arguments?.getString("destination_name")

        // Display the destination name in the TextView
        destinationName?.let {
            binding.etLokasiTujuan.setText(it)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        destinationRef = FirebaseDatabase.getInstance().reference.child("destination")
        koridorRef = FirebaseDatabase.getInstance().reference.child("koridor3b")

        // Inisialiasi lokasi saat ini
        fetchCurrentLocation()

        // Inisialiasi koridor
        fetchKoridor()

        binding.btnCari.setOnClickListener {
            searchDestination()
        }

        // Get the destination name from the arguments
        val destinationName = arguments?.getString("destination_name")

        // Display the destination name in the TextInputEditText
        destinationName?.let {
            binding.etLokasiTujuan.setText(it)
        }

    }

    private fun searchDestination() {
        val destinationName = binding.etLokasiTujuan.text.toString()
        destinationRef.orderByChild("name").equalTo(destinationName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    binding.gridResultHalte.visibility = View.VISIBLE
                    binding.gridResultHalte2.visibility = View.VISIBLE
                    binding.tvHasil.visibility = View.GONE

                    // Lokasi Tujuan ditemukan
                    for (dataSnapshot in snapshot.children) {
                        val destination = dataSnapshot.getValue(Destination::class.java)
                        destination?.let {
                            val destinationLatitude = it.latitude?.toDouble() ?: 0.0
                            val destinationLongitude = it.longitude?.toDouble() ?: 0.0

                            Log.d("Destination", "Latitude: $destinationLatitude, Longitude: $destinationLongitude")

                            // Cari halte terdekat dari Lokasi Awal
                            val (nearestStartHalte, x) = findNearestHalte(currentLatitude, currentLongitude, koridorList)
                            Log.d("NearesHalte", nearestStartHalte.toString())
                            if (nearestStartHalte != null) {
                                val nearestHalteName = nearestStartHalte.name
                                binding.tvStartHalte.text = nearestHalteName
                                Log.d("NearestStartHalte", "Nama Halte Terdekat: $nearestHalteName")
                            } else {
                                Log.d("NearestStartHalte", "Tidak ada halte terdekat ditemukan")
                            }

                            // Cari halte terdekat dari Lokasi Tujuan
                            val (nearestDestHalte, xx) = findNearestHalte(
                                destinationLatitude,
                                destinationLongitude, koridorList)
                            if (nearestDestHalte != null) {
                                val nearestHalteName = nearestDestHalte.name
                                binding.tvDestinationHalte.text = nearestHalteName
                                Log.d("NearestDestinationHalte", "Nama Halte Terdekat: $nearestHalteName")
                            } else {
                                Log.d("NearestDestinationHalte", "Tidak ada halte terdekat ditemukan")
                            }


                            // Hitung jarak Halte Awal ke Halte Tujuan
                            val distance = haversine(currentLatitude, currentLongitude, destinationLatitude, destinationLongitude)
                            binding.tvDistanceEst.text = String.format("Jarak: %.2f km", distance / 1000.0)
                            Log.d("Distance", "Jarak: ${distance/1000} km")

                            // Hitung durasi dalam menit (misalnya kecepatan rata-rata 30 km/jam)
                            val speedKmph = 30.0
                            val duration = distance / (speedKmph * 1000.0 / 60.0)
                            binding.tvTimeEst.text = String.format("Estimasi: %.0f menit", duration)
                            Log.d("Duration", "Durasi: $duration menit")
                        }
                    }
                } else {
                    // Lokasi Tujuan tidak ditemukan
                    binding.gridResultHalte.visibility = View.GONE
                    binding.gridResultHalte2.visibility = View.GONE
                    binding.tvHasil.visibility = View.VISIBLE
                    binding.tvHasil.text = "Rute tidak ditemukan"

                    Log.d("Destination", "Rute tidak ditemukan")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Destination", "Data retrieval canceled: ${error.message}")
            }
        })
    }

    private fun fetchKoridor() {

        koridorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                koridorList.clear()

                for (koridorSnapshot in snapshot.children) {
                    val name = koridorSnapshot.child("name").getValue(String::class.java)
                    val latitude = koridorSnapshot.child("latitude").getValue(Double::class.java)
                    val longitude = koridorSnapshot.child("longitude").getValue(Double::class.java)

                    if (name != null && latitude != null && longitude != null) {
                        val koridor = Koridor(name, latitude, longitude)
                        koridorList.add(koridor)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchCurrentLocation() {

        // get current location
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        currentLatitude = location.latitude
                        currentLongitude = location.longitude
                        Log.d("CurrentLocation", "Latitude: $currentLatitude, Longitude: $currentLongitude")

                        binding.etLokasiAwal.setText("Lokasi Saat Ini")
                        binding.etLokasiAwal.setOnFocusChangeListener { _, hasFocus ->
                            if (hasFocus) {
                                binding.etLokasiAwal.text?.clear()
                            } else {
                                binding.etLokasiAwal.setText("Lokasi Saat Ini")
                            }
                        }

                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
    }

    private fun findNearestHalte(currentLat: Double, currentLon: Double, koridorList: List<Koridor>): Pair<Koridor?, Double> {

        Log.d("FindNearestHalte", "KoridorList: $koridorList")
        Log.d("FindNearestHalte", "CurrentLat: $currentLat")
        Log.d("FindNearestHalte", "CurrentLon: $currentLon")

        var nearestDistance = Double.MAX_VALUE
        var nearestHalte: Koridor? = null

        for (koridor in koridorList) {
            val distance = haversine(currentLat, currentLon, koridor.latitude ?: 0.0, koridor.longitude ?: 0.0)
            if (distance < nearestDistance) {
                nearestDistance = distance
                nearestHalte = koridor
            }
        }

        Log.d("FindNearestHalte", "Nearest Halte: $nearestHalte")

        return Pair(nearestHalte, nearestDistance)
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Radius bumi dalam kilometer
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c * 1000
    }
}