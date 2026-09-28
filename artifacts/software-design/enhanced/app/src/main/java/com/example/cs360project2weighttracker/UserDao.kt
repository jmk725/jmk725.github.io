package com.example.cs360project2weighttracker

import android.content.ContentValues
import android.content.Context
import java.security.MessageDigest

class UserDao(context: Context) {

    private val dbHelper = AppDbHelper(context)

    fun userExists(username: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            AppDbHelper.T_USERS,
            arrayOf(AppDbHelper.C_ID),
            "${AppDbHelper.C_USERNAME}=?",
            arrayOf(username),
            null,
            null,
            null
        )

        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    fun createUser(username: String, password: String): Boolean {
        if (userExists(username)) return false

        // Store a hash instead of the user's plain-text password
        val hashedPassword = hashPassword(password)

        val values = ContentValues().apply {
            put(AppDbHelper.C_USERNAME, username)
            put(AppDbHelper.C_PASSWORD, hashedPassword)
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
            null,
            null,
            null
        )

        if (!cursor.moveToFirst()) {
            cursor.close()
            return false
        }

        val storedPasswordHash = cursor.getString(0)
        cursor.close()

        // Hash the entered password and compare hashes
        return storedPasswordHash == hashPassword(password)
    }

    /**
     * Converts a password into a SHA-256 hash so the original
     * plain-text password is not stored in the database.
     */
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(password.toByteArray(Charsets.UTF_8))

        return bytes.joinToString("") { "%02x".format(it) }
    }
}