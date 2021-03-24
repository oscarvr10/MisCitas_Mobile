package com.ronalverey.myappointments.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog.Builder
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.ronalverey.myappointments.R
import com.ronalverey.myappointments.databinding.ActivityCreateAppointmentBinding
import com.ronalverey.myappointments.io.ApiService
import com.ronalverey.myappointments.model.Specialty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class CreateAppointmentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateAppointmentBinding
    private var selectedCalendar = Calendar.getInstance()
    private var selectedTimeRadioButton: RadioButton? = null

    private val apiService: ApiService by lazy {
        ApiService.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeControls()
    }

    private fun initializeControls() {
        binding.layoutStep1.btnNext.setOnClickListener {
            if (binding.layoutStep1.etDescription.text.toString().length < 3) {
                binding.layoutStep1.etDescription.error = getString(R.string.validate_appointment_description)
            } else {
                binding.layoutStep1.cardStep1.visibility = View.GONE
                binding.layoutStep2.cardStep2.visibility = View.VISIBLE
            }
        }

        binding.layoutStep2.btnNext2.setOnClickListener {
            when {
                binding.layoutStep2.etScheduledDate.text.toString().isEmpty() ->
                    binding.layoutStep2.etScheduledDate.error = getString(R.string.validate_appointment_date)

                selectedTimeRadioButton == null ->
                    Snackbar.make(binding.createAppntLayout, getString(R.string.validate_appointment_time), Snackbar.LENGTH_SHORT).show()

                else -> {
                    showAppointmentDataToConfirm()
                    binding.layoutStep2.cardStep2.visibility = View.GONE
                    binding.layoutStep3.cardStep3.visibility = View.VISIBLE
                }
            }
        }

        binding.layoutStep3.btnConfirm.setOnClickListener {
            Toast.makeText(this, getString(R.string.saved_appointment), Toast.LENGTH_SHORT).show()
            //finish()
        }

        loadSpecialties()

        val doctorOptions = arrayOf("Doctor A", "Doctor B", "Doctor C")
        binding.layoutStep2.spinnerDoctors.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, doctorOptions)
    }

    private fun loadSpecialties() {
        var call = apiService.getSpecialties()
        call.enqueue(object: Callback<ArrayList<Specialty>>{
            override fun onResponse(call: Call<ArrayList<Specialty>>, response: Response<ArrayList<Specialty>>) {
                if (response.isSuccessful){ // status 200...300
                    var specialties = response.body()
                    val specialtyOptions = ArrayList<String>()
                    specialties?.forEach {
                        specialtyOptions.add(it.name)
                    }
                    binding.layoutStep1.spinnerSpecialties.adapter = ArrayAdapter<String>(this@CreateAppointmentActivity, android.R.layout.simple_list_item_1, specialtyOptions)
                }
            }

            override fun onFailure(call: Call<ArrayList<Specialty>>, t: Throwable) {
                Toast.makeText(this@CreateAppointmentActivity, getString(R.string.error_loading_specialties), Toast.LENGTH_SHORT).show()
                finish()
            }

        })
    }

    private fun showAppointmentDataToConfirm() {
        binding.layoutStep3.tvConfirmDescription.text = binding.layoutStep1.etDescription.text.toString()
        binding.layoutStep3.tvConfirmSpecialty.text = binding.layoutStep2.spinnerDoctors.selectedItem.toString()

        val selectedTypeId = binding.layoutStep1.radioGroupType.checkedRadioButtonId
        val selectedType = binding.layoutStep1.radioGroupType.findViewById<RadioButton>(selectedTypeId)

        binding.layoutStep3.tvConfirmType.text =selectedType.text.toString()
        binding.layoutStep3.tvConfirmDoctor.text = binding.layoutStep1.spinnerSpecialties.selectedItem.toString()
        binding.layoutStep3.tvConfirmScheduledDate.text = binding.layoutStep2.etScheduledDate.text.toString()
        binding.layoutStep3.tvConfirmScheduledTime.text =  selectedTimeRadioButton?.text.toString()
    }

    fun onClickScheduledDate(v: View) {
        val year = selectedCalendar.get(Calendar.YEAR)
        val month = selectedCalendar.get(Calendar.MONTH)
        val dayOfMonth = selectedCalendar.get(Calendar.DAY_OF_MONTH)

        val listener = DatePickerDialog.OnDateSetListener { view, y, m, d ->
            selectedCalendar.set(y, m, d)
            binding.layoutStep2.etScheduledDate.setText(resources.getString(R.string.date_format, y, (m + 1).toDigits(), d.toDigits()))
            displayTimeRadioButtons()
        }
        binding.layoutStep2.etScheduledDate.error = null
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

    private fun displayTimeRadioButtons() {
        removeTimeRadioButtons()
        var hours = arrayOf("3:00 PM", "3:30 PM", "4:00 PM", "4:30 PM")
        var goToLeft = true

        hours.forEach {
            var radioButton = RadioButton(this)
            radioButton.id = View.generateViewId() //disponible desde Android API 17
            radioButton.text = it

            radioButton.setOnClickListener { view ->
                selectedTimeRadioButton?.isChecked = false
                selectedTimeRadioButton = view as RadioButton?
                selectedTimeRadioButton?.isChecked = true
            }

            if (goToLeft)
                binding.layoutStep2.radioGroupLeft.addView(radioButton)
            else
                binding.layoutStep2.radioGroupRight.addView(radioButton)

            goToLeft = !goToLeft
        }
    }

    private fun removeTimeRadioButtons() {
        selectedTimeRadioButton = null
        binding.layoutStep2.radioGroupLeft.removeAllViews()
        binding.layoutStep2.radioGroupRight.removeAllViews()
    }

    private fun Int.toDigits() = if (this > 9) this.toString() else "0$this"

    override fun onBackPressed() {
        when {
            binding.layoutStep3.cardStep3.visibility == View.VISIBLE -> {
                binding.layoutStep3.cardStep3.visibility = View.GONE
                binding.layoutStep2.cardStep2.visibility = View.VISIBLE
            }
            binding.layoutStep2.cardStep2.visibility == View.VISIBLE -> {
                binding.layoutStep2.cardStep2.visibility = View.GONE
                binding.layoutStep1.cardStep1.visibility = View.VISIBLE
            }
            binding.layoutStep1.cardStep1.visibility == View.VISIBLE -> {
                val builder = Builder(this)
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
}