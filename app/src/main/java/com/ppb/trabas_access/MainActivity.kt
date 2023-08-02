package com.ppb.trabas_access

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ppb.trabas_access.fragments.HomeFragment
import com.ppb.trabas_access.fragments.ProfileFragment
import com.ppb.trabas_access.fragments.RouteFragment
import com.ppb.trabas_access.fragments.ScanFragment
import com.ppb.trabas_access.fragments.ScheduleFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavbar: BottomNavigationView = findViewById(R.id.bottom_navbar_button)
        val floatBottom: FloatingActionButton = findViewById(R.id.floating_scan_button)

        makeCurrentFragment(HomeFragment())

        // Bottom Navibar
        bottomNavbar.setOnItemSelectedListener {
            item ->
            when (item.itemId) {
                R.id.home -> makeCurrentFragment(HomeFragment())
                R.id.schedule -> makeCurrentFragment(ScheduleFragment())
                R.id.route -> makeCurrentFragment(RouteFragment())
                R.id.profile -> makeCurrentFragment(ProfileFragment())
            }
            true
        }
        floatBottom.setOnClickListener { showBottomDialog() }
    }

    private fun makeCurrentFragment(fragment: Fragment) {

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_navbar, fragment)
        fragmentTransaction.commit()
    }

    // Scan Fragment
    private fun showBottomDialog() {
        val scanFragment = ScanFragment()
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_navbar, scanFragment)
        fragmentTransaction.addToBackStack(null) // Tambahkan fragment ke back stack agar dapat kembali ke fragment sebelumnya
        fragmentTransaction.commit()
    }

}