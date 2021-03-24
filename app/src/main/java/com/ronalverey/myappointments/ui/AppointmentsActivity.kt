package com.ronalverey.myappointments.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ronalverey.myappointments.databinding.ActivityAppointmentBinding
import com.ronalverey.myappointments.model.Appointment

class AppointmentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppointmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val appointments = ArrayList<Appointment>()
        appointments.add(
                Appointment(1, "Medico Test 1", "10/01/2021", "11:00 AM")
        )
        appointments.add(
                Appointment(2, "Medico Test 2", "20/01/2021", "1:00 PM")
        )
        appointments.add(
                Appointment(3, "Medico Test 3", "05/02/2021", "3:00 PM")
        )
        appointments.add(
                Appointment(4, "Medico Test 4", "24/03/2021", "7:00 AM")
        )

        binding.rvAppointments.layoutManager = LinearLayoutManager(this)
        binding.rvAppointments.adapter = AppointmentAdapter(appointments)
    }
}