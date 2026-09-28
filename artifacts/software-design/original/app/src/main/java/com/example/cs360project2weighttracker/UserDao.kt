package com.example.cs360project2weighttracker

import android.content.ContentValues
import android.content.Context

class UserDao(context: Context) {

    private val dbHelper = AppDbHelper(context)

    fun userExists(username: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            AppDbHelper.T_USERS,
            arrayOf(AppDbHelper.C_ID),
            "${AppDbHelper.C_USERNAME}=?",
            arrayOf(username),
            null, null, null
        )

        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    fun createUser(username: String, password: String): Boolean {
        if (userExists(username)) return false

        val values = ContentValues().apply {
            put(AppDbHelper.C_USERNAME, username)
            put(AppDbHelper.C_PASSWORD, password)
            put(AppDbHelper.C_CREATED_AT, System.currentTimeMillis())
        }

        val db = dbHelper.writableDatabase
        return db.insert(AppDbHelper.T_USERS, null, values) != -1L
    }

    fun validateLogin(username: String, password: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            AppDbHelper.T_USERS,
            arrayOf(AppDbHelper.C_PASSWORD),
            "${AppDbHelper.C_USERNAME}=?",
            arrayOf(username),
            null, null, null
        )

        if (!cursor.moveToFirst()) {
            cursor.close()
            return false
        }

        val storedPassword = cursor.getString(0)
        cursor.close()

        return storedPassword == password
    }
}