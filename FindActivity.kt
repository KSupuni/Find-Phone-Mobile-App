package com.example.findphone

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.telephony.SmsManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.IntentFilter
import android.provider.Telephony
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.widget.TextView
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.provider.Settings.Global
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.drawable.DrawableCompat

class FindActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_REQUEST_SEND_SMS = 123
    }
    private lateinit var subscriptionInfoList: List<SubscriptionInfo>
    private lateinit var smsReceiver: SMSReceiver

    // Variables to pass to RemoteMode activity
    private var blevel: String = ""
    private var mData: String = ""
    private var gps: String = ""
    private var wifi: String = ""
    private var pmode: String = ""
    private var aSync: String = ""
    private var volume: String = ""
    private var flash: String = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        supportActionBar?.hide()

        smsReceiver = SMSReceiver()

        // Register SMSReceiver to receive SMS_RECEIVED_ACTION broadcasts
        val intentFilter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        registerReceiver(smsReceiver, intentFilter)


        val btnRemoteMode = findViewById<Button>(R.id.button2)
        val btnCheck = findViewById<Button>(R.id.btnCheck)
        val etPhoneNumber = findViewById<EditText>(R.id.etPhoneNumber)
        val securityCode = findViewById<EditText>(R.id.etSecurityCodeCheck)

        // Initialize the SubscriptionInfo list
        val subscriptionManager = getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList

        btnRemoteMode.visibility= View.GONE

        btnCheck.setOnClickListener {
            val phoneNumber =etPhoneNumber.text.toString()
            val securityCode = securityCode.text.toString()

            val fullcode = securityCode+"CHECK"

            // Check for permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, request it
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), PERMISSION_REQUEST_SEND_SMS)
            } else {
                // Permission already granted, send the message
                sendSMS(phoneNumber, fullcode)
            }


        }

        btnRemoteMode.setOnClickListener{


            val intent = Intent(this,RemoteMode::class.java)

            val phoneNumber =etPhoneNumber.text.toString()
            val securityCode = securityCode.text.toString()


            // Pass the variables to the RemoteMode activity
            intent.putExtra("blevel", blevel)
            intent.putExtra("mData", mData)
            intent.putExtra("gps", gps)
            intent.putExtra("wifi", wifi)
            intent.putExtra("pmode", pmode)
            intent.putExtra("aSync", aSync)
            intent.putExtra("volume", volume)
            intent.putExtra("flash", flash)
            intent.putExtra("phoneNumber", phoneNumber)
            intent.putExtra("securityCode", securityCode)

            Toast.makeText(this, "mData in FindActivity = $mData", Toast.LENGTH_SHORT).show()
            startActivity(intent)


        }

    }
    private fun sendSMS(phoneNumber: String, message: String) {
        try {
            if (subscriptionInfoList.size > 1) {
                // Show SIM selection dialog
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = android.net.Uri.parse("smsto:$phoneNumber")
                intent.putExtra("sms_body", message)
                intent.putExtra("simSlot", 0) // Set the SIM slot index (0 for the first SIM)
                startActivity(intent)
            } else {
                // Only one SIM, send message directly
                val smsManager = android.telephony.SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                Toast.makeText(this, "Message sent successfully.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send message.", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }


    public fun display(blevel: String, mData : String, gps:String, wifi:String, pmode:String, aSync:String, volume:String,flashLight:String) {

        val batteryLevel = findViewById<TextView>(R.id.tvBatteryLevelOutput)
        val mobileDataOutput = findViewById<TextView>(R.id.tvMobileDateOutput)
        val gpsOutput = findViewById<TextView>(R.id.tvGpsOutput)
        val wifiOutput = findViewById<TextView>(R.id.tvWifiOutput)
        val profileMode = findViewById<TextView>(R.id.tvSoundModeOutput)
        val autoSyncOutput = findViewById<TextView>(R.id.tvAutoSyncOutput)
        val phoneVolumeOutput = findViewById<TextView>(R.id.tvPhoneVolumeOutput)

        batteryLevel.text = blevel
        mobileDataOutput.text = mData
        gpsOutput.text = gps
        wifiOutput.text = wifi
        profileMode.text = pmode
        autoSyncOutput.text = aSync
        phoneVolumeOutput.text = volume

        // Assume you have an ImageView with id "imageView" in your activity_main.xml layout
        val imageView = findViewById<ImageView>(R.id.checked_status)

        // Assume you have a drawable named "my_drawable" in your resources
        val drawable = resources.getDrawable(R.drawable.check_status_color, null)

        // Change the color of the drawable
        val color = Color.GREEN
        val modifiedDrawable = changeDrawableColor(drawable, color)

        // Set the modified drawable to the ImageView
        imageView.setImageDrawable(modifiedDrawable)

        val btnRemoteMode = findViewById<Button>(R.id.button2)
        btnRemoteMode.visibility= View.VISIBLE
        //Toast.makeText(this, "mData in FindActivity in display = $mData Count = $count", Toast.LENGTH_LONG).show()

        this@FindActivity.mData = mobileDataOutput.text as String
        this@FindActivity.gps = gpsOutput.text as String
        this@FindActivity.wifi = wifiOutput.text as String
        this@FindActivity.pmode = profileMode.text as String
        this@FindActivity.aSync = autoSyncOutput.text as String
        this@FindActivity.volume = phoneVolumeOutput.text as String
        this@FindActivity.flash = flashLight

    }


    override fun onDestroy() {
        super.onDestroy()

        // Unregister the SMSReceiver when the activity is destroyed
        unregisterReceiver(smsReceiver)
    }
    fun changeDrawableColor(drawable: Drawable, color: Int): Drawable {
        // Wrap the drawable with DrawableCompat to make it mutable
        val wrappedDrawable = DrawableCompat.wrap(drawable).mutate()

        // Set the color using a ColorStateList
        val colorStateList = ColorStateList.valueOf(color)
        DrawableCompat.setTintList(wrappedDrawable, colorStateList)

        // Return the modified drawable
        return wrappedDrawable
    }



}