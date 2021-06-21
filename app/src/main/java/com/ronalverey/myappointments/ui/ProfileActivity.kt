package com.ronalverey.myappointments.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.ronalverey.myappointments.PreferenceHelper
import com.ronalverey.myappointments.PreferenceHelper.get
import com.ronalverey.myappointments.R
import com.ronalverey.myappointments.databinding.ActivityProfileBinding
import com.ronalverey.myappointments.io.ApiService
import com.ronalverey.myappointments.io.response.GenericResponse
import com.ronalverey.myappointments.model.User
import com.ronalverey.myappointments.util.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    private val apiService by lazy {
        ApiService.create()
    }

    private val preferences by lazy {
        PreferenceHelper.defaultPrefs(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val jwt = preferences["jwt", ""]
        val authHeader = "Bearer $jwt"

        var call = apiService.getUser(authHeader)
        call.enqueue(object : Callback<GenericResponse<User>>{
            override fun onResponse(
                call: Call<GenericResponse<User>>,
                response: Response<GenericResponse<User>>
            ) {
                if(response.isSuccessful){
                    var user = response.body()
                    if(user != null)
                        displayProfileData(user.data)
                }
            }

            override fun onFailure(call: Call<GenericResponse<User>>, t: Throwable) {
                toast(t.localizedMessage)
            }
        })
    }

    private fun displayProfileData(user: User) {
        binding.etName.setText(user.name)
        binding.etAddress.setText(user.address)
        binding.etPhone.setText(user.phone)

        binding.pbProfile.visibility = View.GONE
        binding.llProfile.visibility = View.VISIBLE
        
        binding.btnSaveProfile.setOnClickListener{
            saveProfile()
        }
    }

    private fun saveProfile(){
        val jwt = preferences["jwt", ""]
        val authHeader = "Bearer $jwt"
        val name = binding.etName.text.toString()
        val phone = binding.etPhone.text.toString()
        val address = binding.etAddress.text.toString()

        if(name.length < 4){
            binding.inputLayoutName.error = getString(R.string.error_profile_name)
            return;
        }

        val call = apiService.postUser(authHeader, name, phone, address)
        call.enqueue(object: Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                toast(getString(R.string.profile_saved_message))
                finish()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                toast(t.localizedMessage)
            }

        })

    }
}