package com.ppb.trabas_access.fragments

import android.Manifest
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ppb.trabas_access.R
import com.ppb.trabas_access.adapter.CarouselAdapter
import com.ppb.trabas_access.databinding.FragmentHomeBinding
import com.ppb.trabas_access.model.dao.Destination
import com.ppb.trabas_access.model.dao.Koridor
import com.ppb.trabas_access.model.dao.Users
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var usersRef: DatabaseReference
    private lateinit var destinationRef: DatabaseReference
    private lateinit var koridor3b: DatabaseReference
    private lateinit var helloUSerEventListener: ValueEventListener
    private lateinit var textCarouselRunnable: CarouselRunnable
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var carouselAdapter: CarouselAdapter
    private val carouselHandler = Handler(Looper.getMainLooper())
    private val carouselTextList: MutableList<Destination> = mutableListOf()
    private var carouselImages: MutableList<Destination> = mutableListOf()
    private val koridor3bList: MutableList<Koridor> = mutableListOf()
    private var currentIndex = 0
    private val locationPermissionCode = 123

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mapView = binding.mapViewHalteNearby

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usersRef = FirebaseDatabase.getInstance().reference.child("users")
        val currentUser = FirebaseAuth.getInstance().currentUser

        destinationRef = FirebaseDatabase.getInstance().reference.child("destination")

        // Hello User Text
        helloUSerEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userSnapshot = snapshot.child(currentUser?.uid ?: "")
                val user = userSnapshot.getValue(Users::class.java)

                user?.let { userData ->
                    val fullName = userData.fullname
                    val firstName = fullName?.split(" ")?.firstOrNull()?.orEmpty()

                    // Membuat teks "Halo, User" menjadi dua bagian
                    val helloUserText = "Halo, $firstName\nMau jalan-jalan kemana nih?"
                    val spannableHelloUser = SpannableString(helloUserText)

                    // Ubah warna pada teks "firstName" menjadi merah
                    if (firstName != null) {
                        spannableHelloUser.setSpan(
                            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.trabas_red)),
                            6, // indeks karakter pertama pada "firstName" (dimulai dari 0)
                            6 + firstName.length, // indeks karakter terakhir pada "firstName" + 6
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    binding.haloUser.text = spannableHelloUser
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        usersRef.addValueEventListener(helloUSerEventListener)

        // Destination Text Carousel
        fetchDestinationTexts()

        val intervalDuration = 5000L

        startTextCarouselAnimation(intervalDuration)

        // Map
        setupMap()

        // refresh location
        binding.ibRefreshLocation.setOnClickListener {
            fetchCurrentLocationAndUpdateMap()
        }

        // Destination Image Carousel
        fetchCarouselImages()
    }

    // Fetch Destination Texts
    private fun fetchDestinationTexts() {

        destinationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                carouselTextList.clear()
                if (snapshot.exists()) {
                    for (destinationSnapshot in snapshot.children) {
                        val destination = destinationSnapshot.getValue(Destination::class.java)
                        destination?.let {
                            carouselTextList.add(it)
                        }
                    }
                } else {
                    carouselTextList.add(Destination("Baturaden"))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Carousel Text Animation
    private fun startTextCarouselAnimation(intervalDuration: Long) {

        val handler = Handler(requireContext().mainLooper)

        textCarouselRunnable = CarouselRunnable(intervalDuration, requireContext())
        handler.postDelayed(textCarouselRunnable, intervalDuration)
    }

    private inner class CarouselRunnable(private val intervalDuration: Long, private val context: Context) : Runnable {
        override fun run() {
            if (carouselTextList.isNotEmpty()) {
                val slideOutAnimation: Animation = AnimationUtils.loadAnimation(context, R.anim.slide_out_to_top_tv)
                slideOutAnimation.duration = 500

                slideOutAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(p0: Animation?) {}
                    override fun onAnimationEnd(p0: Animation?) {
                        currentIndex = (currentIndex + 1) % carouselTextList.size
                        binding.tvDestinationSearch.text = carouselTextList[currentIndex].name

                        val slideInAnimation: Animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_from_bottom_tv)
                        slideInAnimation.duration = 500
                        binding.tvDestinationSearch.startAnimation(slideInAnimation)

                        carouselHandler.postDelayed(textCarouselRunnable, intervalDuration)
                    }

                    override fun onAnimationRepeat(p0: Animation?) {}
                })

                binding.tvDestinationSearch.startAnimation(slideOutAnimation)
            } else {
                carouselHandler.postDelayed(this, intervalDuration)
            }
        }
    }

    private fun setupMap() {

        Configuration.getInstance().load(context, requireActivity().getSharedPreferences("OpenStreetMap", MODE_PRIVATE))

        mapView.setTileSource(TileSourceFactory.MAPNIK)

        // set coordinates center

        // Banyumas
        val lat = -7.4546306
        val long = 109.0037606

        val startPoint = GeoPoint(lat, long)
        val zoomLevel = 17.5
        mapView.controller.setCenter(startPoint)
        mapView.controller.setZoom(zoomLevel)

        koridor3b = FirebaseDatabase.getInstance().reference.child("koridor3b")

        koridor3b.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                koridor3bList.clear()

                for (koridorSnapshot in snapshot.children) {
                    val name = koridorSnapshot.child("name").getValue(String::class.java)
                    val latitude = koridorSnapshot.child("latitude").getValue(Double::class.java)
                    val longitude = koridorSnapshot.child("longitude").getValue(Double::class.java)

                    if (name != null && latitude != null && longitude != null) {
                        val koridor = Koridor(name, latitude, longitude)
                        koridor3bList.add(koridor)
                    }
                }

                // get current location
                fetchCurrentLocationAndUpdateMap()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fungsi untuk menghitung jarak
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

    // Fungsi untuk mencari halte/koridor terdekat
    private fun findNearestHalte(currentLat: Double, currentLon: Double, koridorList: List<Koridor>): Pair<Koridor?, Double> {
        var nearestDistance = Double.MAX_VALUE
        var nearestHalte: Koridor? = null

        for (koridor in koridorList) {
            val distance = haversine(currentLat, currentLon, koridor.latitude ?: 0.0, koridor.longitude ?: 0.0)
            if (distance < nearestDistance) {
                nearestDistance = distance
                nearestHalte = koridor
            }
        }

        return Pair(nearestHalte, nearestDistance)
    }

    private fun markNearestHalteAndCurrentLocation(currentLatitude: Double, currentLongitude: Double) {

        val (nearestHalte, nearestDistance) = findNearestHalte(currentLatitude, currentLongitude, koridor3bList)

        val markerPoints = mutableListOf<GeoPoint>()

        // marker halte terdekat
        nearestHalte?.let {
            val nearestPoint = GeoPoint(it.latitude ?: 0.0, it.longitude ?: 0.0)
            markerPoints.add(nearestPoint)
            val nearestMarker = Marker(mapView)

            nearestMarker.position = nearestPoint
            nearestMarker.title = it.name
            nearestMarker.subDescription = "Jarak: ${String.format("%.2f", nearestDistance)} m"
            nearestMarker.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_bus_stop_blue)
            nearestMarker.setAnchor(0.5F, 0F)
            mapView.overlays.add(nearestMarker)
            mapView.controller.animateTo(nearestPoint)

            nearestMarker.showInfoWindow()
        }

        // marker lokasi saat ini
        val currentLocationMarker = Marker(mapView)
        val currentLocationPoint = GeoPoint(currentLatitude, currentLongitude)

        markerPoints.add(currentLocationPoint)
        currentLocationMarker.position = currentLocationPoint
        currentLocationMarker.title = "Lokasi Anda"

        currentLocationMarker.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_location_marker)
        mapView.overlays.add(currentLocationMarker)

        if (markerPoints.isNotEmpty()) {
            val boundingBox = BoundingBox.fromGeoPoints(markerPoints)
            mapView.zoomToBoundingBox(boundingBox, true, 100) // Ganti angka 100 dengan durasi animasi zooming (dalam milidetik)
        }

        // Menampilkan info window (label) pada marker lokasi saat ini
//        currentLocationMarker.showInfoWindow()

        mapView.invalidate() // Untuk memperbarui tampilan peta

    }

    private fun fetchCurrentLocationAndUpdateMap() {

        // get current location
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val currentLatitude = location.latitude
                        val currentLongitude = location.longitude
                        Log.d("Location", "Latitude: $currentLatitude, Longitude: $currentLongitude")

                        mapView.overlays.clear()

                        markNearestHalteAndCurrentLocation(currentLatitude, currentLongitude)
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Jika izin lokasi belum diberikan, minta izin secara runtime
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
    }

//    private val imageCarouselRunnable = object : Runnable {
//        override fun run() {
//            var currentPosition = binding.viewPagerDestinasi.currentItem
//            if (currentPosition == carouselImages.size - 1) {
//                currentPosition = 0
//            } else {
//                currentPosition++
//            }
//            binding.viewPagerDestinasi.setCurrentItem(currentPosition, true)
//            carouselHandler.postDelayed(this, 5000)
//        }
//    }

//    private val carouselPageChangeListener = object : ViewPager.OnPageChangeListener {
//        override fun onPageScrollStateChanged(state: Int) {}
//
//        override fun onPageScrolled(
//            position: Int,
//            positionOffset: Float,
//            positionOffsetPixels: Int
//        ) {}
//
//        override fun onPageSelected(position: Int) {
//            // Ketika mencapai halaman terakhir, atur halaman pertama sebagai halaman berikutnya
//            if (position == carouselImages.size - 1) {
//                binding.viewPagerDestinasi.setCurrentItem(0, false)
//            }
//        }
//    }

    private fun fetchCarouselImages() {

        destinationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                carouselImages.clear()

                for (destinationSnapshot in snapshot.children) {
                    val destination = destinationSnapshot.getValue(Destination::class.java)
                    destination?.let {
                        if (!it.name.isNullOrEmpty() && !it.image.isNullOrEmpty()) {
                            carouselImages.add(it)
                        }
                    }
                }

                carouselAdapter = CarouselAdapter(carouselImages, binding.viewPagerDestinasi)
                binding.viewPagerDestinasi.adapter = carouselAdapter
                startCarouselAutoScroll()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun startCarouselAutoScroll() {
        val carouselHandler = Handler(Looper.getMainLooper())
        val imageCarouselRunnable = object : Runnable {
            override fun run() {
                val currentPosition = binding.viewPagerDestinasi.currentItem
                val nextPosition = if (currentPosition + 1 < carouselAdapter.itemCount) currentPosition + 1 else 0
                binding.viewPagerDestinasi.setCurrentItem(nextPosition, true)
                carouselHandler.postDelayed(this, 6000)
            }
        }
        carouselHandler.postDelayed(imageCarouselRunnable, 6000)
    }

    override fun onResume() {
        super.onResume()
        fetchDestinationTexts()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        usersRef.removeEventListener(helloUSerEventListener)
        carouselHandler.removeCallbacks(textCarouselRunnable, startCarouselAutoScroll())
    }

//    fun onNotificationButtonClicked(view: View) {}
}
