package com.ppb.trabas_access

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ppb.trabas_access.databinding.ActivityRegisterBinding
import com.ppb.trabas_access.model.dao.Users

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val btnRegister = binding.btnRegister
        btnRegister.setOnClickListener {
            val fullname = binding.fieldFullnameRegister.text.toString()
            val email = binding.fieldEmailReg.text.toString()
            val phone = ""
            val password = binding.fieldPasswordReg.text.toString()
            val confirmPass = binding.fieldConfirmPassword.text.toString()

            if (fullname.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPass.isNotEmpty()) {

                if (password == confirmPass) {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {task ->

                        if (task.isSuccessful) {
                            val currentUser = firebaseAuth.currentUser
                            val userId = currentUser?.uid

                            val userRef = database.getReference("users")
                            val user = Users(fullname, email, phone, password)

                            if (userId != null) {
                                userRef.child(userId).setValue(user)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Pendaftaran Berhasil", Toast.LENGTH_SHORT).show()

                                        val intent = Intent(this, LoginActivity::class.java)
                                        startActivity(intent)
                                    }
                                    .addOnFailureListener {
                                        exception ->
                                        Toast.makeText(this, "Gagal menyimpan data pengguna: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Pendaftaran Gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Kata sandi tidak cocok!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Kolom tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            }
        }

        val btnLogin: TextView = binding.tvLoginButton
        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}