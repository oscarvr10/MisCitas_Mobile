package com.ronalverey.myappointments

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.ronalverey.myappointments.PreferenceHelper.defaultPrefs
import com.ronalverey.myappointments.PreferenceHelper.get
import com.ronalverey.myappointments.PreferenceHelper.set
import com.ronalverey.myappointments.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        val prefs = defaultPrefs(this)
        if (prefs["session", false])
            goToMenuActivity()

        binding!!.btnLogin.setOnClickListener { v ->
            createSessionPreferences()
            goToMenuActivity()
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

    private fun goToMenuActivity() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun createSessionPreferences() {
        //val preferences = getSharedPreferences("general", Context.MODE_PRIVATE);
        //val editor = preferences.edit();
        //editor.putBoolean("session", true).apply();
        val prefs = defaultPrefs(this)
        prefs["session"] = true
    }
}