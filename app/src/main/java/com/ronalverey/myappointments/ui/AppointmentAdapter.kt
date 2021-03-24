package com.ronalverey.myappointments.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ronalverey.myappointments.R
import com.ronalverey.myappointments.databinding.ItemAppointmentBinding
import com.ronalverey.myappointments.model.Appointment

class AppointmentAdapter(private val appointments: ArrayList<Appointment>)
    : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemAppointmentBinding.bind(itemView)

        fun bind(appointment: Appointment)= with(binding) {
            tvAppointmentId.text = root.context.getString(R.string.item_appointment_id, appointment.id)
            tvDoctorName.text = appointment.doctorName
            tvScheduledDate.text = root.context.getString(R.string.item_appointment_date, appointment.scheduledDate)
            tvScheduledTime.text = root.context.getString(R.string.item_appointment_time, appointment.scheduledTime)
        }
    }

    // Inflates XML items
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_appointment, parent, false)
        )
    }

    // Binds data to viewholder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var appointment = appointments[position]
        holder.bind(appointment)
    }

    // Returns number of elements
    override fun getItemCount() = appointments.size
}