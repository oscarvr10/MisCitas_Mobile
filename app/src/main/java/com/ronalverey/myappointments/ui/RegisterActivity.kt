package com.ronalverey.myappointments.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ronalverey.myappointments.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvGoToLogin.setOnClickListener { view ->
            val intent = Intent(view.context, MainActivity::class.java)
            startActivity(intent)
        }

        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
    }
}