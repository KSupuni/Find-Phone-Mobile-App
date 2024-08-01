package com.example.findphone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.telephony.SmsManager
import android.widget.Toast
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class SIMStateReceiver : BroadcastReceiver() {



    override fun onReceive(context: Context?, intent: Intent?) {

        Toast.makeText(context, "Function Activated", Toast.LENGTH_SHORT).show()
        val telephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val newSimState = telephonyManager.simState

        CoroutineScope(Dispatchers.IO).launch {
            val appDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "my_db").build()
            val settingsDao = appDatabase.settingsDao()
            val settings = settingsDao.getSettingsById(1)

            withContext(Dispatchers.Main) {
                settings?.let {
                    val phoneNumber = it.phoneNumber
                    sendReplyMessage(context,phoneNumber,"This message from new number!")
                }
            }
        }


    }
    private fun sendReplyMessage(context: Context, sender: String, message: String) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(sender, null, message, null, null)
    }
}
