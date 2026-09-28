package com.example.cs360project2weighttracker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var weightDao: WeightDao
    private lateinit var adapter: WeightAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Open Notifications screen
        findViewById<Button>(R.id.btnNotifications).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        // Edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        weightDao = WeightDao(this)

        // RecyclerView grid setup
        val recycler = findViewById<RecyclerView>(R.id.recyclerWeights)
        recycler.layoutManager = GridLayoutManager(this, 2)

        adapter = WeightAdapter(
            items = emptyList(),
            onClick = { entry -> showAddEditDialog(entry) },       // UPDATE
            onLongClick = { entry -> confirmDelete(entry) }        // DELETE
        )
        recycler.adapter = adapter

        // FAB = CREATE
        findViewById<FloatingActionButton>(R.id.fabAddWeight).setOnClickListener {
            showAddEditDialog(null)
        }

        refreshGrid()
    }

    /** Read all entries and display them */
    private fun refreshGrid() {
        adapter.submit(weightDao.getAll())
    }

    /**
     * entry == null -> CREATE
     * entry != null -> UPDATE
     */
    private fun showAddEditDialog(entry: WeightEntry?) {

        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_edit_weight, null)

        val etDate = dialogView.findViewById<EditText>(R.id.etDate)
        val etWeight = dialogView.findViewById<EditText>(R.id.etWeight)
        val etNotes = dialogView.findViewById<EditText>(R.id.etNotes)

        // Prefill when editing
        if (entry != null) {
            etDate.setText(entry.date)
            etWeight.setText(entry.weight.toString())
            etNotes.setText(entry.notes ?: "")
        }

        val title = if (entry == null) "Add Weight" else "Edit Weight"

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->

                val date = etDate.text.toString().trim()
                val weightText = etWeight.text.toString().trim()
                val notes = etNotes.text.toString().trim().ifBlank { null }

                // Basic validation
                if (date.isEmpty() || weightText.isEmpty()) {
                    Toast.makeText(this, "Date and weight required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val weight = weightText.toDoubleOrNull()
                if (weight == null || weight <= 0) {
                    Toast.makeText(this, "Enter a valid weight", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (entry == null) {
                    // CREATE
                    val id = weightDao.insert(date, weight, notes)
                    if (id != -1L) {
                        Toast.makeText(this, "Entry added", Toast.LENGTH_SHORT).show()
                        // Trigger SMS if goal reached
                        maybeSendGoalSms(weight)
                    } else {
                        Toast.makeText(this, "Could not add entry", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // UPDATE
                    val rows = weightDao.update(entry.id, date, weight, notes)
                    if (rows > 0) {
                        Toast.makeText(this, "Entry updated", Toast.LENGTH_SHORT).show()
                        // Trigger SMS if goal reached
                        maybeSendGoalSms(weight)
                    } else {
                        Toast.makeText(this, "Could not update entry", Toast.LENGTH_SHORT).show()
                    }
                }

                refreshGrid()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /** Delete with confirmation dialog */
    private fun confirmDelete(entry: WeightEntry) {
        AlertDialog.Builder(this)
            .setTitle("Delete entry?")
            .setMessage("${entry.date} • ${entry.weight} lbs")
            .setPositiveButton("Delete") { _, _ ->
                val rows = weightDao.delete(entry.id)
                if (rows > 0) {
                    Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show()
                    refreshGrid()
                } else {
                    Toast.makeText(this, "Could not delete entry", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * SMS notification trigger:
     * If the user has set a goal weight and phone number, and newWeight <= goal,
     * send an SMS *only if permission is granted*. If denied, app continues normally.
     */
    private fun maybeSendGoalSms(newWeight: Double) {
        val settings = SettingsDao(this).getSettings()

        // If goal not set or phone not set, do nothing
        if (settings.goalWeight <= 0) return
        if (settings.phoneNumber.isBlank()) return

        // Trigger condition: reached/under goal
        if (newWeight <= settings.goalWeight) {
            if (SmsUtil.canSendSms(this)) {
                try {
                    SmsUtil.sendSms(
                        settings.phoneNumber,
                        "Goal reached! Current weight: $newWeight lbs (Goal: ${settings.goalWeight} lbs)"
                    )
                    Toast.makeText(this, "Goal reached SMS sent!", Toast.LENGTH_SHORT).show()
                } catch (ex: Exception) {
                    // Emulator/device might block SMS; app should not crash
                    Toast.makeText(
                        this,
                        "SMS failed to send (device/emulator).",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Permission denied: app continues without SMS
                Toast.makeText(this, "Goal reached (SMS disabled).", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


