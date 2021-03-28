package com.ronalverey.myappointments.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ronalverey.myappointments.databinding.ActivityAppointmentBinding
import com.ronalverey.myappointments.io.ApiService
import com.ronalverey.myappointments.model.Appointment
import com.ronalverey.myappointments.PreferenceHelper.defaultPrefs
import com.ronalverey.myappointments.PreferenceHelper.get
import com.ronalverey.myappointments.PreferenceHelper.set
import com.ronalverey.myappointments.util.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppointmentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppointmentBinding
    private val apiService: ApiService by lazy {
        ApiService.create()
    }

    private val preferences by lazy {
        defaultPrefs(this)
    }

    private val appoinmentAdapter = AppointmentAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadAppointments()

        binding.rvAppointments.layoutManager = LinearLayoutManager(this)
        binding.rvAppointments.adapter = appoinmentAdapter
    }

    private fun loadAppointments() {
        val jwt = preferences["jwt", ""]
        val call = apiService.getAppointments("Bearer $jwt")
        call.enqueue(object: Callback<ArrayList<Appointment>> {
            override fun onResponse(call: Call<ArrayList<Appointment>>, response: Response<ArrayList<Appointment>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        appoinmentAdapter.appointments = it
                        appoinmentAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onFailure(call: Call<ArrayList<Appointment>>, t: Throwable) {
                toast(t.localizedMessage)
            }

        })
    }
}