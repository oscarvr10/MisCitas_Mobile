package com.ronalverey.myappointments.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.ronalverey.myappointments.PreferenceHelper.defaultPrefs
import com.ronalverey.myappointments.PreferenceHelper.get
import com.ronalverey.myappointments.PreferenceHelper.set
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

    companion object {
        private const val TAG = "MenuActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var storeToken = intent.getBooleanExtra("store_token", false)
        if (storeToken){
            storeToken()
        }

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

    private fun storeToken(){
        val jwt = preferences["jwt", ""]
        var authHeader = "Bearer $jwt"

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val deviceToken = task.result
            val call = apiService.postToken(authHeader,deviceToken)
            call.enqueue(object: Callback<Void>{
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful){
                        Log.d(TAG, "Token registrado correctamente")
                    }else{
                        Log.d(TAG, "Error al registrar Token")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    toast(t.localizedMessage)
                }

            })
        })
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
