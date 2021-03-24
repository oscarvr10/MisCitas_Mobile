package com.ronalverey.myappointments.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ronalverey.myappointments.PreferenceHelper.defaultPrefs
import com.ronalverey.myappointments.PreferenceHelper.set
import com.ronalverey.myappointments.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCreateAppointment.setOnClickListener {
            val intent = Intent(this, CreateAppointmentActivity::class.java)
            startActivity(intent)
        }

        binding.btnMyAppointments.setOnClickListener {
            val intent = Intent(this, AppointmentsActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            clearSessionPreferences()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun clearSessionPreferences() {
        //val preferences = getSharedPreferences("general", Context.MODE_PRIVATE)
        //val editor = preferences.edit()
        //editor.putBoolean("session", false).apply()

        val prefs = defaultPrefs(this)
        prefs["session"] = false
    }
}