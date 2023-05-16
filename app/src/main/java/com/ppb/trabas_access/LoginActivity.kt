package com.ppb.trabas_access

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
            val savedLogin = getSharedPreferences("Login", MODE_PRIVATE)
            val editSavedLogin = savedLogin.edit()
            if (savedLogin.getString("Status", "Off")=="On"){
                startActivity(Intent(this, MainActivity::class.java))
            }
            }
            btnRegister.setOnClickListener {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }

        }
    }
}