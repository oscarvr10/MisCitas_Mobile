package com.ronalverey.myappointments

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.ronalverey.myappointments.databinding.ActivityCreateAppointmentBinding
import java.util.*

class  CreateAppointmentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateAppointmentBinding
    private var selectedCalendar = Calendar.getInstance()
    private var selectedRadioButton: RadioButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeControls()
    }

    private fun initializeControls (){
        binding.btnNext.setOnClickListener{
            if (binding.etDescription.text.toString().length < 3){
                binding.etDescription.error = getString(R.string.validate_appointment_description)
            } else {
                binding.cardStep1.visibility = View.GONE
                binding.cardStep2.visibility = View.VISIBLE
            }
        }

        binding.btnConfirm.setOnClickListener{
            Toast.makeText(this, getString(R.string.saved_appointment), Toast.LENGTH_SHORT).show()
            //finish()
        }
        val specialtyOptions = arrayOf("Especialidad A", "Especialidad B", "Especialidad C")
        binding.spinnerSpecialties.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, specialtyOptions)

        val doctorOptions = arrayOf("Doctor A", "Doctor B", "Doctor C")
        binding.spinnerDoctors.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, doctorOptions)
    }

    fun onClickScheduledDate(v: View) {
        val year = selectedCalendar.get(Calendar.YEAR)
        val month = selectedCalendar.get(Calendar.MONTH)
        val dayOfMonth = selectedCalendar.get(Calendar.DAY_OF_MONTH)

        val listener = DatePickerDialog.OnDateSetListener { view, y, m, d ->
            selectedCalendar.set(y, m, d)
            binding.etScheduledDate.setText(resources.getString(R.string.date_format,y, (m + 1).toDigits(), d.toDigits()))
            displayRadioButtons()
        }
        createDatePickerDialog(listener, year, month, dayOfMonth)
    }

    private fun createDatePickerDialog(listener: DatePickerDialog.OnDateSetListener, year: Int, month: Int, dayOfMonth: Int) {
        val datePickerDialog = DatePickerDialog(this, listener, year, month, dayOfMonth)
        val datePicker = datePickerDialog.datePicker
        val calendar = Calendar.getInstance()

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        datePicker.minDate = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 365)
        datePicker.maxDate = calendar.timeInMillis

        datePickerDialog.show()
    }

    private fun displayRadioButtons() {
        removeRadioButtons()
        var hours = arrayOf("3:00 PM", "3:30 PM", "4:00 PM",  "4:30 PM")
        var goToLeft = true

        hours.forEach {
            var radioButton = RadioButton(this)
            radioButton.id = View.generateViewId() //disponible desde Android API 17
            radioButton.text = it

            radioButton.setOnClickListener { view ->
                selectedRadioButton?.isChecked = false
                selectedRadioButton = view as RadioButton?
                selectedRadioButton?.isChecked = true
            }

            if(goToLeft)
                binding.radioGroupLeft.addView(radioButton)
            else
                binding.radioGroupRight.addView(radioButton)

            goToLeft = !goToLeft
        }
    }

    private fun removeRadioButtons() {
        selectedRadioButton = null
        binding.radioGroupLeft.removeAllViews()
        binding.radioGroupRight.removeAllViews()
    }

    private fun Int.toDigits () = if(this > 9) this.toString() else "0$this"

    override fun onBackPressed() {
        if (binding.cardStep2.visibility == View.VISIBLE){
            binding.cardStep2.visibility = View.GONE
            binding.cardStep1.visibility = View.VISIBLE
        } else if (binding.cardStep1.visibility == View.GONE) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.dialog_create_appointment_exit_title))
            builder.setMessage(getString(R.string.dialog_create_appointment_exit_message))
            builder.setPositiveButton(getString(R.string.dialog_create_appointment_exit_positive_btn)) { dialog, which ->
                finish()
            }
            builder.setNegativeButton(getString(R.string.dialog_create_appointment_exit_negative_btn)) { dialog, which ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }
    }
}