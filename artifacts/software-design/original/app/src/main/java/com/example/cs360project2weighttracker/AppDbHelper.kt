package com.example.cs360project2weighttracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDbHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {

        // USERS TABLE (login info)
        db.execSQL(
            """
            CREATE TABLE $T_USERS (
                $C_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $C_USERNAME TEXT UNIQUE NOT NULL,
                $C_PASSWORD TEXT NOT NULL,
                $C_CREATED_AT INTEGER NOT NULL
            );
            """.trimIndent()
        )

        // WEIGHTS TABLE (daily entries)
        db.execSQL(
            """
            CREATE TABLE $T_WEIGHTS (
                $C_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $C_DATE TEXT NOT NULL,
                $C_WEIGHT REAL NOT NULL,
                $C_NOTES TEXT
            );
            """.trimIndent()
        )

        // SETTINGS TABLE (goal + phone)
        db.execSQL(
            """
            CREATE TABLE $T_SETTINGS (
                $C_ID INTEGER PRIMARY KEY,
                $C_GOAL REAL NOT NULL,
                $C_PHONE TEXT
            );
            """.trimIndent()
        )

        // Seed default settings row (id = 1)
        val seed = ContentValues().apply {
            put(C_ID, 1)
            put(C_GOAL, 0.0)
            put(C_PHONE, "")
        }
        db.insert(T_SETTINGS, null, seed)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $T_USERS")
        db.execSQL("DROP TABLE IF EXISTS $T_WEIGHTS")
        db.execSQL("DROP TABLE IF EXISTS $T_SETTINGS")
        onCreate(db)
    }

    companion object {
        const val DB_NAME = "daily_weight.db"
        const val DB_VERSION = 1

        // Tables
        const val T_USERS = "users"
        const val T_WEIGHTS = "weights"
        const val T_SETTINGS = "settings"

        // Common column
        const val C_ID = "id"

        // Users
        const val C_USERNAME = "username"
        const val C_PASSWORD = "password"
        const val C_CREATED_AT = "created_at"

        // Weights
        const val C_DATE = "entry_date"   // "YYYY-MM-DD"
        const val C_WEIGHT = "weight_lbs"
        const val C_NOTES = "notes"

        // Settings
        const val C_GOAL = "goal_weight_lbs"
        const val C_PHONE = "phone_number"
    }
}
