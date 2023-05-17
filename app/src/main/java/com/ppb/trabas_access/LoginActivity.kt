package com.ppb.trabas_access

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.ppb.trabas_access.R.layout.activity_login)
            val savedLogin = getSharedPreferences("Login", MODE_PRIVATE)
            val editSavedLogin = savedLogin.edit()
            if (savedLogin.getString("Status", "Off")=="On"){
                startActivity(Intent(this, MainActivity::class.java))
            }

        val btnLogin: MaterialButton = findViewById(com.ppb.trabas_access.R.id.btnLogin)

        btnLogin.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}