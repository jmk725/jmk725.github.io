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

    companion object {
        private const val MIN_WEIGHT = 50.0
        private const val MAX_WEIGHT = 1000.0

    }

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
            onClick = { entry -> showAddEditDialog(entry) },
            onLongClick = { entry -> confirmDelete(entry) }
        )

        recycler.adapter = adapter

        // Floating action button creates a new weight entry
        findViewById<FloatingActionButton>(R.id.fabAddWeight).setOnClickListener {
            showAddEditDialog(null)
        }

        refreshGrid()
    }

    /** Read all entries and display them. */
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

        // Prefill fields when editing an existing entry
        if (entry != null) {
            etDate.setText(entry.date)
            etWeight.setText(entry.weight.toString())
            etNotes.setText(entry.notes ?: "")
        }

        val title = if (entry == null) "Add Weight" else "Edit Weight"

        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        /*
         * Override the Save button so invalid data does not close the
         * dialog. The record is saved only after all validation passes.
         */
        dialog.setOnShowListener {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

                val date = etDate.text.toString().trim()
                val weightText = etWeight.text.toString().trim()
                val notes = etNotes.text.toString().trim().ifBlank { null }

                // Required-field validation
                if (date.isEmpty()) {
                    etDate.error = "Date is required"
                    return@setOnClickListener
                }

                if (weightText.isEmpty()) {
                    etWeight.error = "Weight is required"
                    return@setOnClickListener
                }

                // Date must use YYYY-MM-DD and represent a real date
                if (!isValidDate(date)) {
                    etDate.error = "Enter a valid date as YYYY-MM-DD"
                    return@setOnClickListener
                }

                // Weight must be numeric
                val weight = weightText.toDoubleOrNull()

                if (weight == null) {
                    etWeight.error = "Weight must be a number"
                    return@setOnClickListener
                }

                // Testable weight range
                if (weight < MIN_WEIGHT || weight > MAX_WEIGHT) {
                    etWeight.error =
                        "Weight must be between 50 and 1000 lbs"
                    return@setOnClickListener
                }

                if (entry == null) {

                    // CREATE
                    val id = weightDao.insert(date, weight, notes)

                    if (id != -1L) {
                        Toast.makeText(
                            this,
                            "Entry added",
                            Toast.LENGTH_SHORT
                        ).show()

                        maybeSendGoalSms(weight)
                        refreshGrid()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(
                            this,
                            "Could not add entry",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {

                    // UPDATE
                    val rows = weightDao.update(
                        entry.id,
                        date,
                        weight,
                        notes
                    )

                    if (rows > 0) {
                        Toast.makeText(
                            this,
                            "Entry updated",
                            Toast.LENGTH_SHORT
                        ).show()

                        maybeSendGoalSms(weight)
                        refreshGrid()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(
                            this,
                            "Could not update entry",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        dialog.show()
    }

    /**
     * Validates both the YYYY-MM-DD format and the actual calendar date.
     */
    private fun isValidDate(date: String): Boolean {
        if (!date.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) {
            return false
        }

        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        formatter.isLenient = false

        return try {
            formatter.parse(date)
            true
        } catch (ex: java.text.ParseException) {
            false
        }
    }

    /** Delete with confirmation dialog. */
    private fun confirmDelete(entry: WeightEntry) {

        AlertDialog.Builder(this)
            .setTitle("Delete entry?")
            .setMessage("${entry.date} • ${entry.weight} lbs")
            .setPositiveButton("Delete") { _, _ ->

                val rows = weightDao.delete(entry.id)

                if (rows > 0) {
                    Toast.makeText(
                        this,
                        "Entry deleted",
                        Toast.LENGTH_SHORT
                    ).show()

                    refreshGrid()
                } else {
                    Toast.makeText(
                        this,
                        "Could not delete entry",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * If the user has configured a goal weight and phone number,
     * send an SMS when the entered weight reaches or falls below
     * the goal. If SMS permission is unavailable, the application
     * continues normally.
     */
    private fun maybeSendGoalSms(newWeight: Double) {

        val settings = SettingsDao(this).getSettings()

        if (settings.goalWeight <= 0) return
        if (settings.phoneNumber.isBlank()) return

        if (newWeight <= settings.goalWeight) {

            if (SmsUtil.canSendSms(this)) {

                try {

                    SmsUtil.sendSms(
                        settings.phoneNumber,
                        "Goal reached! Current weight: $newWeight lbs " +
                                "(Goal: ${settings.goalWeight} lbs)"
                    )

                    Toast.makeText(
                        this,
                        "Goal reached SMS sent!",
                        Toast.LENGTH_SHORT
                    ).show()

                } catch (ex: Exception) {

                    Toast.makeText(
                        this,
                        "SMS failed to send (device/emulator).",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } else {

                Toast.makeText(
                    this,
                    "Goal reached (SMS disabled).",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}


