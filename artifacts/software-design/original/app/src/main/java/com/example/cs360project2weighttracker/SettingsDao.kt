package com.example.cs360project2weighttracker

import android.content.ContentValues
import android.content.Context

data class Settings(
    val goalWeight: Double,
    val phoneNumber: String
)

class SettingsDao(context: Context) {
    private val dbHelper = AppDbHelper(context)

    fun getSettings(): Settings {
        val db = dbHelper.readableDatabase
        val c = db.query(
            AppDbHelper.T_SETTINGS,
            arrayOf(AppDbHelper.C_GOAL, AppDbHelper.C_PHONE),
            "${AppDbHelper.C_ID}=?",
            arrayOf("1"),
            null, null, null
        )

        val settings = if (c.moveToFirst()) {
            Settings(
                goalWeight = c.getDouble(0),
                phoneNumber = c.getString(1) ?: ""
            )
        } else {
            Settings(0.0, "")
        }

        c.close()
        return settings
    }

    fun saveSettings(goalWeight: Double, phoneNumber: String): Int {
        val values = ContentValues().apply {
            put(AppDbHelper.C_GOAL, goalWeight)
            put(AppDbHelper.C_PHONE, phoneNumber)
        }

        return dbHelper.writableDatabase.update(
            AppDbHelper.T_SETTINGS,
            values,
            "${AppDbHelper.C_ID}=?",
            arrayOf("1")
        )
    }
}