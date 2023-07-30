package com.ppb.trabas_access.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ppb.trabas_access.LoginActivity
import com.ppb.trabas_access.R
import com.ppb.trabas_access.databinding.FragmentProfileBinding
import com.ppb.trabas_access.model.dao.Users

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var usersRef: DatabaseReference
    private lateinit var currentUser: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true // Retain the Fragment instance across configuration changes
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        usersRef = FirebaseDatabase.getInstance().reference.child("users").child(currentUser)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(Users::class.java)

                // Check if phone number is empty or null
                if (user?.phone.isNullOrEmpty()) {
                    // Show "Mohon lengkapi profil Anda" message
                    binding.tvNotifProfile.visibility = View.VISIBLE
                    binding.tvNotifProfile.text = getString(R.string.missing_phone_notification)

                    // Hide phone number
                    binding.tvPhone.visibility = View.GONE

                    // Show "Tambahkan No. Handphone" button
                    binding.tvPhone.visibility = View.VISIBLE
                    binding.tvPhone.text = getString(R.string.add_phone_button)

                    // Set click listener for "Tambahkan No. Handphone" button
                    binding.tvPhone.setOnClickListener {

                        val editProfileFragment = EditProfileFragment()
                        parentFragmentManager
                            .beginTransaction()
                            .replace(R.id.frame_navbar, editProfileFragment)
                            .addToBackStack(null) // Add the fragment to the back stack so it can be navigated back
                            .commit()
                    }
                } else {
                    // Hide "Mohon lengkapi profil Anda" message
                    binding.tvNotifProfile.background = null
                    binding.tvNotifProfile.text = ""

                    // Show phone number
                    binding.tvPhone.visibility = View.VISIBLE
                    binding.tvPhone.text = user?.phone
                    binding.tvPhone.setTypeface(null, Typeface.NORMAL)
                }

                // Update other views with user data if needed
                binding.tvName.text = user?.fullname ?: ""
                binding.tvEmail.text = user?.email ?: ""

                // Edit Profile
                binding.btnEditProfile.setOnClickListener {
                    val editProfileFragment = EditProfileFragment()
                    parentFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_navbar, editProfileFragment)
                        .addToBackStack(null) // Add the fragment to the back stack so it can be navigated back
                        .commit()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Profile Fragment", "Error getting user data: ", databaseError.toException())
            }
        })

        // Logout
        binding.btnLogoutProfile.setOnClickListener {
            performLogout()
        }

    }

    private fun performLogout() {

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Konfirmasi Logout")
        builder.setMessage("Apakah Anda yakin ingin keluar?")
        builder.setPositiveButton("Ya") { _, _ ->
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)

            requireActivity().finish()
        }
        builder.setNegativeButton("Tidak", null)

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}