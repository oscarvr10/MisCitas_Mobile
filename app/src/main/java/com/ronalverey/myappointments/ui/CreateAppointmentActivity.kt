package com.ronalverey.myappointments.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.ronalverey.myappointments.PreferenceHelper.defaultPrefs
import com.ronalverey.myappointments.PreferenceHelper.get
import com.ronalverey.myappointments.R
import com.ronalverey.myappointments.databinding.ActivityCreateAppointmentBinding
import com.ronalverey.myappointments.io.ApiService
import com.ronalverey.myappointments.io.response.BooleanResponse
import com.ronalverey.myappointments.model.Doctor
import com.ronalverey.myappointments.model.Schedule
import com.ronalverey.myappointments.model.Specialty
import com.ronalverey.myappointments.util.toast
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

    private val preferences by lazy {
        defaultPrefs(this)
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
            performStoreAppointment()
        }

        loadSpecialties()
        onSelectedSpecialtyChanged()
        onSelectedDoctorChanged()

        val doctorOptions = arrayOf("Doctor A", "Doctor B", "Doctor C")
    }

    private fun performStoreAppointment() {
        binding.layoutStep3.btnConfirm.isClickable = false
        val jwt = preferences["jwt", ""]
        val authHeader = "Bearer $jwt"
        val description = binding.layoutStep3.tvConfirmDescription.text.toString()
        val specialty = binding.layoutStep1.spinnerSpecialties.selectedItem as Specialty
        val doctor = binding.layoutStep2.spinnerDoctors.selectedItem as Doctor
        val scheduledDate = binding.layoutStep3.tvConfirmScheduledDate.text.toString()
        val scheduledTime = binding.layoutStep3.tvConfirmScheduledTime.text.toString()
        val type = binding.layoutStep3.tvConfirmType.text.toString()

        val call = apiService.postAppointment(authHeader, description, specialty.id, doctor.id, scheduledDate, scheduledTime, type)
        call.enqueue(object: Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                if(response.isSuccessful) {
                    toast(getString(R.string.create_appointment_success))
                    finish()
                } else {
                    toast(getString(R.string.create_appointment_error))
                    binding.layoutStep3.btnConfirm.isClickable = true
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                toast(t.localizedMessage)
                binding.layoutStep3.btnConfirm.isClickable = true
            }
        })
    }

    private fun onSelectedDoctorChanged() {
        binding.layoutStep2.spinnerDoctors.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(adapter: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedDoctor = adapter?.getItemAtPosition(position) as Doctor
                loadAvailableHours(selectedDoctor.id, binding.layoutStep2.etScheduledDate.text.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        binding.layoutStep2.etScheduledDate.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val selectedDoctor = binding.layoutStep2.spinnerDoctors.selectedItem as Doctor;
                loadAvailableHours(selectedDoctor.id, binding.layoutStep2.etScheduledDate.text.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

    }

    private fun onSelectedSpecialtyChanged() {
        binding.layoutStep1.spinnerSpecialties.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(adapter: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedSpecialty = adapter?.getItemAtPosition(position) as Specialty
                loadDoctorsBySpecialty(selectedSpecialty.id)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun loadAvailableHours(doctorId: Int, date: String) {
        if(date.isNullOrEmpty()){
            return;
        }

        var call = apiService.getAvailableHours(doctorId, date)
        call.enqueue(object: Callback<Schedule>{
            override fun onResponse(call: Call<Schedule>, response: Response<Schedule>) {
                if (response.isSuccessful){ // status 200...300
                    response.body()?.let {
                        var schedule = it
                        schedule?.let {
                            binding.layoutStep2.tvSelectDoctorAndDate.visibility = View.GONE
                            val intervals = schedule.morning + schedule.afternoon
                            val hours = ArrayList<String>()
                            intervals.forEach { interval ->
                                hours.add(interval.start)
                            }
                            displayIntervalRadioBtns(hours)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<Schedule>, t: Throwable) {
                Toast.makeText(this@CreateAppointmentActivity, getString(R.string.error_loading_hours), Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun loadDoctorsBySpecialty(specialtyId: Int) {
        var call = apiService.getDoctorsBySpecialty(specialtyId)
        call.enqueue(object: Callback<ArrayList<Doctor>>{
            override fun onResponse(call: Call<ArrayList<Doctor>>, response: Response<ArrayList<Doctor>>) {
                if (response.isSuccessful){ // status 200...300
                    response.body()?.let {
                        var doctors = it.toMutableList()
                        binding.layoutStep2.spinnerDoctors.adapter = ArrayAdapter<Doctor>(this@CreateAppointmentActivity, android.R.layout.simple_list_item_1, doctors)
                    }
                }
            }

            override fun onFailure(call: Call<ArrayList<Doctor>>, t: Throwable) {
                Toast.makeText(this@CreateAppointmentActivity, getString(R.string.error_loading_doctors), Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun loadSpecialties() {
        var call = apiService.getSpecialties()
        call.enqueue(object: Callback<ArrayList<Specialty>>{
            override fun onResponse(call: Call<ArrayList<Specialty>>, response: Response<ArrayList<Specialty>>) {
                if (response.isSuccessful){ // status 200...300
                    response.body()?.let {
                        var specialties = it.toMutableList()
                        binding.layoutStep1.spinnerSpecialties.adapter = ArrayAdapter<Specialty>(this@CreateAppointmentActivity, android.R.layout.simple_list_item_1, specialties)
                    }
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

    private fun displayIntervalRadioBtns(hours: ArrayList<String>) {
        removeIntervalRadioBtns()
        var goToLeft = true
        if(hours.isNullOrEmpty()){
            binding.layoutStep2.tvNotAvailableHours.visibility = View.VISIBLE
            return;
        }

        binding.layoutStep2.tvNotAvailableHours.visibility = View.GONE
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

    private fun removeIntervalRadioBtns() {
        selectedTimeRadioButton = null
        binding.layoutStep2.radioGroupLeft.removeAllViews()
        binding.layoutStep2.radioGroupRight.removeAllViews()
    }

    private fun Int.toDigits() = if (this > 9) this.toString() else "0$this"
}