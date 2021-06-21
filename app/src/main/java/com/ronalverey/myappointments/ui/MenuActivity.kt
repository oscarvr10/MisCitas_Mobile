package com.ronalverey.myappointments.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import com.ronalverey.myappointments.PreferenceHelper.defaultPrefs
import com.ronalverey.myappointments.PreferenceHelper.get
import com.ronalverey.myappointments.PreferenceHelper.set
import com.ronalverey.myappointments.R
import com.ronalverey.myappointments.databinding.ActivityMenuBinding
import com.ronalverey.myappointments.io.ApiService
import com.ronalverey.myappointments.io.response.GenericResponse
import com.ronalverey.myappointments.model.User
import com.ronalverey.myappointments.util.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding

    private val apiService by lazy {
        ApiService.create()
    }

    private val preferences by lazy {
        defaultPrefs(this)
    }

    private val authHeader by lazy {
        val jwt = preferences["jwt", ""]
        "Bearer $jwt"
    }

    companion object {
        private const val TAG = "MenuActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var storeToken = intent.getBooleanExtra("store_token", false)
        if (storeToken){
            storeToken()
        }
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        binding.btnProfile.setOnClickListener {
            editProfile()
        }

        binding.btnCreateAppointment.setOnClickListener {
            createAppointment(it)
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
        val call = apiService.postLogout(authHeader)
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

    private fun createAppointment(view: View) {
        val call =apiService.getUser(authHeader)
        call.enqueue(object: Callback<GenericResponse<User>>{
            override fun onResponse(call: Call<GenericResponse<User>>, response: Response<GenericResponse<User>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        var user = it.data
                        if(user.phone.length == 10){
                            var intent = Intent(this@MenuActivity, CreateAppointmentActivity::class.java);
                            startActivity(intent);
                        } else{
                            Snackbar.make(view, getString(R.string.phone_required_message), Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
            override fun onFailure(call: Call<GenericResponse<User>>, t: Throwable) {
                toast(t.localizedMessage)
            }
        })
    }

    private fun editProfile() {
        var intent = Intent(this, ProfileActivity::class.java);
        startActivity(intent);
    }
}
