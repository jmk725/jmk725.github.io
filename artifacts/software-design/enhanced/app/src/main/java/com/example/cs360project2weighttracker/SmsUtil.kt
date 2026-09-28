package com.example.cs360project2weighttracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.core.content.ContextCompat

object SmsUtil {
    fun canSendSms(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun sendSms(phone: String, message: String) {
        SmsManager.getDefault().sendTextMessage(phone, null, message, null, null)
    }
}