package com.ronalverey.myappointments.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ronalverey.myappointments.PreferenceHelper.defaultPrefs
import com.ronalverey.myappointments.PreferenceHelper.set
import com.ronalverey.myappointments.PreferenceHelper.get
import com.ronalverey.myappointments.databinding.ActivityMenuBinding
import com.ronalverey.myappointments.io.ApiService
import com.ronalverey.myappointments.util.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MenuActivity : AppCompatActivity() {
    private val apiService by lazy {
        ApiService.create()
    }
    private val preferences by lazy {
        defaultPrefs(this)
    }

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
            performLogout()
        }
    }

    private fun clearSessionPreferences() {
        preferences["jwt"] = ""
    }

    private fun performLogout() {
        val jwt = preferences["jwt", ""]
        val call = apiService.postLogout("Bearer $jwt")
        call.enqueue(object: Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    clearSessionPreferences()

                    val intent = Intent(this@MenuActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                toast(t.localizedMessage)
            }

        })
    }
}