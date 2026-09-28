package com.example.cs360project2weighttracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NotificationsActivity : AppCompatActivity() {

    private lateinit var settingsDao: SettingsDao

    private lateinit var etGoal: EditText
    private lateinit var etPhone: EditText
    private lateinit var tvStatus: TextView

    private val REQ_SMS = 2001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notifications)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        settingsDao = SettingsDao(this)

        etGoal = findViewById(R.id.etGoalWeight)
        etPhone = findViewById(R.id.etPhone)
        tvStatus = findViewById(R.id.tvStatus)

        // Load saved settings
        val s = settingsDao.getSettings()
        if (s.goalWeight > 0) etGoal.setText(s.goalWeight.toString())
        etPhone.setText(s.phoneNumber)

        updateSmsStatusText()

        findViewById<Button>(R.id.btnSaveSettings).setOnClickListener {
            val goalText = etGoal.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            val goal = goalText.toDoubleOrNull()
            if (goal == null || goal <= 0) {
                Toast.makeText(this, "Enter a valid goal weight.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            settingsDao.saveSettings(goal, phone)
            Toast.makeText(this, "Settings saved.", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnEnableSms).setOnClickListener {
            requestSmsPermission()
        }
    }

    private fun requestSmsPermission() {
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED

        if (granted) {
            Toast.makeText(this, "SMS permission already granted.", Toast.LENGTH_SHORT).show()
            updateSmsStatusText()
            return
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.SEND_SMS),
            REQ_SMS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQ_SMS) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (granted) {
                Toast.makeText(this, "SMS permission granted.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "SMS permission denied. App will still work without SMS.", Toast.LENGTH_LONG).show()
            }
            updateSmsStatusText()
        }
    }

    private fun updateSmsStatusText() {
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED
        tvStatus.text = if (granted) "SMS Status: Enabled" else "SMS Status: Disabled"
    }
}