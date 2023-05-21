package com.ppb.trabas_access.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.ppb.trabas_access.LoginActivity
import com.ppb.trabas_access.R

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val myButton = view.findViewById<Button>(R.id.logout)
        myButton.setOnClickListener {
            goToLoginActivity()
        }

        return view
    }

    private fun goToLoginActivity() {
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
    }
}