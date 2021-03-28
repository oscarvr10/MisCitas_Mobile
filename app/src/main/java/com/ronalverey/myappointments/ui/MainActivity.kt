package com.ronalverey.myappointments.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.ronalverey.myappointments.PreferenceHelper.defaultPrefs
import com.ronalverey.myappointments.PreferenceHelper.get
import com.ronalverey.myappointments.PreferenceHelper.set
import com.ronalverey.myappointments.R
import com.ronalverey.myappointments.databinding.ActivityMainBinding
import com.ronalverey.myappointments.io.ApiService
import com.ronalverey.myappointments.io.response.GenericResponse
import com.ronalverey.myappointments.io.response.Login
import com.ronalverey.myappointments.util.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private val apiService: ApiService by lazy {
        ApiService.create()
    }

    //First time it's required it will be initialized just once
    private val snackbar by lazy {
        Snackbar.make(binding!!.mainLayout, R.string.press_back_again, Snackbar.LENGTH_SHORT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        val prefs = defaultPrefs(this)
        if (prefs["jwt", ""].contains("."))
            goToMenuActivity()

        binding!!.btnLogin.setOnClickListener { v ->
            performLogin()
        }

        binding!!.tvGoToRegister.setOnClickListener { v ->
            Toast.makeText(v.context, getString(R.string.please_fill_register_data), Toast.LENGTH_SHORT).show()
            val intent = Intent(v.context, RegisterActivity::class.java)
            startActivity(intent)
        }

        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
    }

    private fun performLogin() {
        val email = binding?.edEmail?.text.toString()
        val password = binding?.edPassword?.text.toString()

        val call = apiService.postLogin(email, password)
        call.enqueue(object: Callback<GenericResponse<Login>>{
            override fun onResponse(call: Call<GenericResponse<Login>>, response: Response<GenericResponse<Login>>) {
                if (response.code() == 200) {
                    val loginResponse = response.body()
                    if (loginResponse == null){
                        toast(getString(R.string.error_login_response))
                        return
                    }
                    if(loginResponse.success){
                        createSessionPreferences(loginResponse.data.jwt)
                        goToMenuActivity()
                    }
                } else if (response.code() == 401) {
                    toast(getString(R.string.error_invalid_credentials))
                } else {
                    toast(getString(R.string.error_login_response))
                }
            }

            override fun onFailure(call: Call<GenericResponse<Login>>, t: Throwable) {
                toast(t.localizedMessage)
            }

        } )
    }

    private fun goToMenuActivity() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun createSessionPreferences(jwt: String) {
        //val preferences = getSharedPreferences("general", Context.MODE_PRIVATE);
        //val editor = preferences.edit();
        //editor.putBoolean("session", true).apply();
        val prefs = defaultPrefs(this)
        prefs["jwt"] = jwt
    }

    override fun onBackPressed() {
        if (snackbar.isShown)
            super.onBackPressed()
        else
            snackbar.show()

    }
}