package com.ronalverey.myappointments.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ronalverey.myappointments.PreferenceHelper
import com.ronalverey.myappointments.PreferenceHelper.set
import com.ronalverey.myappointments.R
import com.ronalverey.myappointments.databinding.ActivityRegisterBinding
import com.ronalverey.myappointments.io.ApiService
import com.ronalverey.myappointments.io.response.GenericResponse
import com.ronalverey.myappointments.io.response.Login
import com.ronalverey.myappointments.util.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private val apiService: ApiService by lazy {
        ApiService.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnConfirmRegister.setOnClickListener{
            performRegister()
        }

        binding.tvGoToLogin.setOnClickListener { view ->
            val intent = Intent(view.context, MainActivity::class.java)
            startActivity(intent)
        }

        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
    }

    private fun performRegister() {
        val name = binding.etRegisterName.text.toString().trim()
        val email = binding.etRegisterEmail.text.toString().trim()
        val password = binding.etRegisterPassword.text.toString()
        val pwdConfirmation = binding.etRegisterPwdConfirmation.text.toString()

        if (name.isNullOrEmpty() || email.isNullOrEmpty()
                || password.isNullOrEmpty() || pwdConfirmation.isNullOrEmpty()) {
            toast(getString(R.string.error_register_empty_fields))
            return;
        }

        if (password != pwdConfirmation) {
            toast(getString(R.string.error_passwords_do_not_match))
            return;
        }

        val call = apiService.postRegister(name, email, password, pwdConfirmation)
        call.enqueue(object: Callback<GenericResponse<Login>>{
            override fun onResponse(call: Call<GenericResponse<Login>>, response: Response<GenericResponse<Login>>) {
                if (response.isSuccessful){
                    response.body()?.let {
                        val registerResponse = it
                        if (registerResponse == null){
                            toast(getString(R.string.error_login_response))
                            return
                        }
                        if(registerResponse.success){
                            createSessionPreferences(registerResponse.data.jwt)
                            toast(getString(R.string.welcome_name, registerResponse.data.user.name))
                            goToMenuActivity()
                        } else {
                            toast(getString(R.string.error_register_validation))
                        }
                    }
                } else {
                    toast(getString(R.string.error_register_validation))
                }
            }

            override fun onFailure(call: Call<GenericResponse<Login>>, t: Throwable) {
                toast(t.localizedMessage)
            }

        })
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
        val prefs = PreferenceHelper.defaultPrefs(this)
        prefs["jwt"] = jwt
    }

}