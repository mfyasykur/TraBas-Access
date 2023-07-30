package com.ppb.trabas_access.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ppb.trabas_access.databinding.FragmentEditProfileBinding
import com.ppb.trabas_access.model.dao.Users

class EditProfileFragment : Fragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var usersRef: DatabaseReference
    private lateinit var currentUser: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        usersRef = FirebaseDatabase.getInstance().reference.child("users").child(currentUser)

        // Read data from Firebase Database
        currentUser.let {
            usersRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(Users::class.java)
                    user?.let {
                        binding.etFullname.setText(it.fullname)
                        binding.etEmail.setText(it.email)
                        binding.etEmail.isEnabled = false
                        binding.etPhone.setText(it.phone)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Edit Profile Fragment", "Error getting user data: ", databaseError.toException())
                }
            })
        }

        // Set click listener for the "BATAL" button
        binding.btnCancel.setOnClickListener {
            // Revert changes in EditText fields to original data
            currentUser.let {
                usersRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val user = dataSnapshot.getValue(Users::class.java)
                        user?.let {
                            binding.etFullname.setText(it.fullname)
                            binding.etPhone.setText(it.phone)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e(TAG, "Error getting user data: ", databaseError.toException())
                    }
                })
            }

            // Go back to the previous fragment (ProfileFragment)
            parentFragmentManager.popBackStack()
        }

        // Set click listener for the "SIMPAN" button
        binding.btnSave.setOnClickListener {
            // Get edited data from EditText fields
            val fullname = binding.etFullname.text.toString()
            val phone = binding.etPhone.text.toString()

            // Create a HashMap to hold the updated data
            val updates = hashMapOf<String, Any?>()
            updates["fullname"] = fullname
            updates["phone"] = phone

            // Update the user's data in the database
            currentUser.let {
                usersRef.updateChildren(updates).addOnCompleteListener { task ->
                    if (isAdded) {
                        if (task.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "Profil berhasil diperbarui!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Terjadi kesalahan saat menyimpan data!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            parentFragmentManager.popBackStack()
        }
    }

    companion object {
        private const val TAG = "EditProfileFragment"
    }
}