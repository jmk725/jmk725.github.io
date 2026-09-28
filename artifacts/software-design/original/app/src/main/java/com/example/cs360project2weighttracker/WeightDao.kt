package com.example.cs360project2weighttracker

import android.content.ContentValues
import android.content.Context

data class WeightEntry(
    val id: Long,
    val date: String,
    val weight: Double,
    val notes: String?
)

class WeightDao(context: Context) {

    private val dbHelper = AppDbHelper(context)

    // CREATE
    fun insert(date: String, weight: Double, notes: String?): Long {
        val values = ContentValues().apply {
            put(AppDbHelper.C_DATE, date)
            put(AppDbHelper.C_WEIGHT, weight)
            put(AppDbHelper.C_NOTES, notes)
        }

        return dbHelper.writableDatabase
            .insert(AppDbHelper.T_WEIGHTS, null, values)
    }

    // UPDATE
    fun update(id: Long, date: String, weight: Double, notes: String?): Int {
        val values = ContentValues().apply {
            put(AppDbHelper.C_DATE, date)
            put(AppDbHelper.C_WEIGHT, weight)
            put(AppDbHelper.C_NOTES, notes)
        }

        return dbHelper.writableDatabase.update(
            AppDbHelper.T_WEIGHTS,
            values,
            "${AppDbHelper.C_ID}=?",
            arrayOf(id.toString())
        )
    }

    // DELETE
    fun delete(id: Long): Int {
        return dbHelper.writableDatabase.delete(
            AppDbHelper.T_WEIGHTS,
            "${AppDbHelper.C_ID}=?",
            arrayOf(id.toString())
        )
    }

    // READ (all items)
    fun getAll(): List<WeightEntry> {
        val list = mutableListOf<WeightEntry>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            AppDbHelper.T_WEIGHTS,
            null,
            null,
            null,
            null,
            null,
            "${AppDbHelper.C_DATE} DESC"
        )

        while (cursor.moveToNext()) {
            list.add(
                WeightEntry(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(AppDbHelper.C_ID)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(AppDbHelper.C_DATE)),
                    weight = cursor.getDouble(cursor.getColumnIndexOrThrow(AppDbHelper.C_WEIGHT)),
                    notes = cursor.getString(cursor.getColumnIndexOrThrow(AppDbHelper.C_NOTES))
                )
            )
        }

        cursor.close()
        return list
    }
}