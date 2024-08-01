package com.example.findphone

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.airbnb.lottie.parser.IntegerParser
import com.google.android.material.textfield.TextInputEditText

class RemoteMode : AppCompatActivity() {

    private lateinit var smsReceiver: SMSReceiver

    private lateinit var switchData: Switch
    private lateinit var switchWifi: Switch
    private lateinit var switchGps: Switch
    private lateinit var switchAutoSync: Switch
    private lateinit var switchFlashLight: Switch
    private lateinit var switchSoundMode: Switch

    private lateinit var playButton: ImageButton
    private lateinit var stopButton: ImageButton

    private lateinit var myImage: ImageView
    private var isPlay: Boolean = true

    private lateinit var subscriptionInfoList: List<SubscriptionInfo>


    var blevel1 ="Off"
    var mData1 = "Off"
    var gps1 = "Off"
    var wifi1 ="Off"
    var pmode1 = "Off"
    var aSync1 = "Off"
    var volume1 = "Off"
    var flash1 = "Off"


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote_mode)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        supportActionBar?.hide()

        smsReceiver = SMSReceiver()

        // Register SMSReceiver to receive SMS_RECEIVED_ACTION broadcasts
        val intentFilter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        registerReceiver(smsReceiver, intentFilter)


        myImage = findViewById(R.id.ivPlayButton)

        // Initialize the SubscriptionInfo list
        val subscriptionManager = getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList

        switchData = findViewById(R.id.sMobiledta)
        switchWifi = findViewById(R.id.sWifi)
        switchGps = findViewById(R.id.sGps)
        switchAutoSync = findViewById(R.id.sAutoSync)
        switchFlashLight = findViewById(R.id.sFlashLight)
        switchSoundMode = findViewById(R.id.sSoundMode)
        val btnSend = findViewById<Button>(R.id.btnSend)
        val btnView = findViewById<Button>(R.id.btnCopy)
        val btnGet = findViewById<Button>(R.id.btnGet)

        // Retrieve the variables sent from FindActivity
        val blevel = intent.getStringExtra("blevel")
        val mData = intent.getStringExtra("mData")
        val gps = intent.getStringExtra("gps")
        val wifi = intent.getStringExtra("wifi")
        val pmode = intent.getStringExtra("pmode")
        val aSync = intent.getStringExtra("aSync")
        val volume = intent.getStringExtra("volume")
        val flash = intent.getStringExtra("flash")
        val phoneNumber = intent.getStringExtra("phoneNumber")
        val securiyCode = intent.getStringExtra("securityCode")





        //Toast.makeText(this, "mData in RemoteMode = $mData", Toast.LENGTH_SHORT).show()

        switchData.isChecked = mData=="On"
        switchWifi.isChecked = wifi=="On"
        switchGps.isChecked = gps=="On"
        switchAutoSync.isChecked = aSync=="On"
        switchSoundMode.isChecked = (volume?.toInt())!! >= 100
        switchFlashLight.isChecked = flash=="On"

        btnSend.setOnClickListener {

            mData1 = when (switchData.isChecked) {
                true -> "On"
                else -> "Off"
            }
            gps1 = when (switchGps.isChecked) {
                true -> "On"
                else -> "Off"
            }
            wifi1 = when (switchWifi.isChecked) {
                true -> "On"
                else -> "Off"
            }
            volume1 = when (switchSoundMode.isChecked) {
                true -> "On"
                else -> "Off"
            }
            aSync1 = when (switchAutoSync.isChecked) {
                true -> "On"
                else -> "Off"
            }
            flash1 = when (switchFlashLight.isChecked) {
                true -> "On"
                else -> "Off"
            }


            var msg = "Security Code : $securiyCode \nMobile Data : $mData1\nGPS : $gps1\n" +
                    "WIFI : $wifi1\nAuto Sync : $aSync1\nVolume : $volume1\nFlash : $flash1\nREPLY"


            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, request it
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS),
                    FindActivity.PERMISSION_REQUEST_SEND_SMS
                )
            } else {
                // Permission already granted, send the message
                sendSMS(phoneNumber.toString(), msg)
            }


        }

        playButton = findViewById(R.id.ivPlayButton)
        playButton.setOnClickListener {
            onPlayButtonClick()
        }

        stopButton = findViewById(R.id.ivStopButton)
        stopButton.setOnClickListener {
            onStopButtonClick()
        }

        btnView.setOnClickListener(){
            val tfLocation = findViewById<TextView>(R.id.tfLocation)

            openLocationInGoogleMaps(tfLocation.text.toString())
        }

        btnGet.setOnClickListener(){
            val phoneNumber = intent.getStringExtra("phoneNumber")
            val securiyCode = intent.getStringExtra("securityCode")


            sendSMS(phoneNumber.toString(), securiyCode+"LOCATION")
        }
    }
    fun openLocationInGoogleMaps(locationString: String) {
        val startIndex = locationString.indexOf("Lat=")
        val endIndex = locationString.indexOf(",", startIndex)
        val latitude = locationString.substring(startIndex + 4, endIndex).toDouble()

        val startIndexLng = locationString.indexOf("Lng=", endIndex)
        val endIndexLng = locationString.indexOf("\n", startIndexLng)
        val longitude = locationString.substring(startIndexLng + 4, endIndexLng).toDouble()

        val uri = Uri.parse("geo:$latitude,$longitude")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")

        // Check if the Google Maps app is installed
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            // Google Maps app is not installed, open in web browser
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"))
            startActivity(webIntent)
        }
    }

    private fun onStopButtonClick() {

        val phoneNumber = intent.getStringExtra("phoneNumber")
        val securiyCode = intent.getStringExtra("securityCode")

        val currentDrawable: Drawable? = playButton.drawable

        if (currentDrawable is VectorDrawable || currentDrawable is StateListDrawable) {
            val pauseDrawableResId = R.drawable.baseline_pause_24
            val playDrawableResId = R.drawable.baseline_play_arrow_24

            val currentDrawableResId = getResourceId(currentDrawable)
            val pauseDrawableResIdFromResources = getResourceId(
                AppCompatResources.getDrawable(this, pauseDrawableResId)
            )

            if (currentDrawableResId == pauseDrawableResIdFromResources) {
                playButton.setImageDrawable(
                    AppCompatResources.getDrawable(this, playDrawableResId)
                )
                showToast("Changed to play icon")
                sendSMS(phoneNumber.toString(), securiyCode+"STOPSOUND")
            } else {
                showToast("Already in play icon state")
            }
        } else {
            showToast("Invalid drawable type")
        }
    }
    private fun getResourceId(drawable: Drawable?): Int {
        return try {
            val field = drawable?.javaClass?.getDeclaredField("mResourceId")
            field?.isAccessible = true
            field?.getInt(drawable) ?: 0
        } catch (e: Exception) {
            0
        }
    }
    public fun displayLocation(location:String){
        Toast.makeText(this, "In Display Location", Toast.LENGTH_SHORT).show()

        val tfLocation = findViewById<TextView>(R.id.tfLocation)
        tfLocation.text = location
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    fun onPlayButtonClick() {
        val currentDrawable: Drawable? = playButton.drawable

        val phoneNumber = intent.getStringExtra("phoneNumber")
        val securiyCode = intent.getStringExtra("securityCode")

        if (currentDrawable is VectorDrawable || currentDrawable is StateListDrawable) {
            val newDrawable = AppCompatResources.getDrawable(this, R.drawable.baseline_pause_24)
            playButton.setImageDrawable(newDrawable)
            sendSMS(phoneNumber.toString(), securiyCode+"PLAYSOUND")

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
}