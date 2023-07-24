package com.ppb.trabas_access.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ppb.trabas_access.R
import com.ppb.trabas_access.databinding.FragmentHomeBinding
import com.ppb.trabas_access.model.dao.Destination
import com.ppb.trabas_access.model.dao.Users

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var usersRef: DatabaseReference
    private lateinit var destinationRef: DatabaseReference
    private lateinit var helloUSerEventListener: ValueEventListener
    private lateinit var textCarouselRunnable: CarouselRunnable
    private val carouselHandler = Handler(Looper.getMainLooper())
    private val carouselTextList = mutableListOf<Destination>()
    private var currentIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
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
                            ForegroundColorSpan(resources.getColor(R.color.trabas_red)),
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

        startCarouselAnimation(intervalDuration)
    }

    override fun onResume() {
        super.onResume()
        fetchDestinationTexts()
    }

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

    private fun startCarouselAnimation(intervalDuration: Long) {

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

    override fun onDestroyView() {
        super.onDestroyView()
        usersRef.removeEventListener(helloUSerEventListener)
        carouselHandler.removeCallbacks(textCarouselRunnable)
    }


//    fun onNotificationButtonClicked(view: View) {}
}
