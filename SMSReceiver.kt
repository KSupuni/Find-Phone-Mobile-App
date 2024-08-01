package com.example.findphone
import android.Manifest
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.os.BatteryManager
import android.provider.Settings
import android.content.ContentResolver
import android.content.pm.PackageManager
import java.io.File
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.android.material.textfield.TextInputEditText


class SMSReceiver : BroadcastReceiver() {





    private var mediaPlayer: MediaPlayer? = null
    private var locationManager: LocationManager? = null
    private var currentLocation: Location? = null
    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "New SMS received!", Toast.LENGTH_SHORT).show()
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {
            Toast.makeText(context, "New SMS received!", Toast.LENGTH_SHORT).show()

            val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val sb = StringBuilder()
            for (message in smsMessages) {
                val sender = message.displayOriginatingAddress
                val messageBody = message.messageBody
                sb.append("Sender: $sender\n")
                sb.append("Message: $messageBody\n")

                // Check if the message text matches the desired format "XXXXFLASHON"
                if (messageBody.endsWith("FLASHON")) {
                    val securityCodeLength = messageBody.length - "FLASHON".length
                    val securityCode = messageBody.substring(0, securityCodeLength)
                    handleMessagesToggle(context, securityCode,messageBody,sender)
                }
                else if(messageBody.endsWith("FLASHOFF"))
                {
                    val securityCodeLength = messageBody.length - "FLASHOFF".length
                    val securityCode = messageBody.substring(0, securityCodeLength)
                    handleMessagesToggle(context, securityCode,messageBody,sender)
                }
                else if(messageBody.endsWith("SOUNDMAX"))
                {
                    val securityCodeLength = messageBody.length - "SOUNDMAX".length
                    val securityCode = messageBody.substring(0, securityCodeLength)
                    handleMessagesToggle(context, securityCode,messageBody,sender)
                }
                else if(messageBody.endsWith("PLAYSOUND"))
                {
                    val securityCodeLength = messageBody.length - "PLAYSOUND".length
                    val securityCode = messageBody.substring(0, securityCodeLength)
                    handleMessagesToggle(context, securityCode,messageBody,sender)
                }
                else if(messageBody.endsWith("STOPSOUND"))
                {
                    Toast.makeText(context, "Stop", Toast.LENGTH_SHORT).show()
                    val securityCodeLength = messageBody.length - "STOPSOUND".length
                    val securityCode = messageBody.substring(0, securityCodeLength)
                    handleMessagesToggle(context, securityCode,messageBody,sender)
                }
                else if(messageBody.endsWith("CHECK"))
                {
                    val securityCodeLength = messageBody.length - "CHECK".length
                    val securityCode = messageBody.substring(0, securityCodeLength)

                    checkStatus(context, sender, securityCode)

                }
                else if(messageBody.endsWith("DONE"))
                {
                    val msg = messageBody

                    // Split the input string by newline ("\n") to get individual lines
                    val lines = msg.split("\n")

                    var batteryLevel = ""
                    var mobileData = ""
                    var gps = ""
                    var wifi = ""
                    var soundMode = ""
                    var ringingToneVolume = ""
                    var autoSync = ""
                    var flashLight =""

                    // Iterate through each line and extract the required information
                    for (line in lines) {
                        // Split each line by ":" to separate the key and value
                        val parts = line.split(":")

                        if (parts.size == 2) {
                            val key = parts[0].trim()
                            val value = parts[1].trim()

                            when (key) {
                                "Battery" -> batteryLevel = value
                                "Mobile Data" -> mobileData = value
                                "GPS" -> gps = value
                                "WIFI" -> wifi = value
                                "Sound Mode" -> soundMode = value
                                "Ringing Tone Volume" -> ringingToneVolume = value
                                "Auto Sync" -> autoSync = value
                                "Flash Light" -> flashLight = value


                            }

                        }
                        // Call the display function in the FindActivity instance
                        (context as? FindActivity)?.display(batteryLevel,mobileData,gps,wifi,soundMode,autoSync,ringingToneVolume,flashLight)
                    }

                }
                else if(messageBody.endsWith("REPLY"))
                {

                    val msg = messageBody

                    // Split the input string by newline ("\n") to get individual lines
                    val lines = msg.split("\n")

                    var mobileData = ""
                    var gps = ""
                    var wifi = ""
                    var ringingToneVolume = ""
                    var autoSync = ""
                    var flashLight =""
                    var securityCode = ""

                    // Iterate through each line and extract the required information
                    for (line in lines) {
                        // Split each line by ":" to separate the key and value
                        val parts = line.split(":")

                        if (parts.size == 2) {
                            val key = parts[0].trim()
                            val value = parts[1].trim()

                            when (key) {
                                "Security Code" -> securityCode = value
                                "Mobile Data" -> mobileData = value
                                "GPS" -> gps = value
                                "WIFI" -> wifi = value
                                "Volume" -> ringingToneVolume = value
                                "Auto Sync" -> autoSync = value
                                "Flash" -> flashLight = value


                            }

                        }

                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        val appDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "my_db").build()
                        val settingsDao = appDatabase.settingsDao()
                        val settings = settingsDao.getSettingsById(1)

                        withContext(Dispatchers.Main) {
                            settings?.let {
                                Toast.makeText(context, "$securityCode == ${it.securityCode}", Toast.LENGTH_SHORT).show()
                                if (securityCode == it.securityCode)
                                {
                                    Toast.makeText(context, "In the if", Toast.LENGTH_SHORT).show()
                                    if(ringingToneVolume=="On"){
                                        Toast.makeText(context, "turnOnSoundMax", Toast.LENGTH_SHORT).show()
                                        turnOnSoundMax(context)
                                    }

                                    if(flashLight=="On"){
                                        Toast.makeText(context, "In the flash on", Toast.LENGTH_SHORT).show()
                                        turnOnFlashlight(context)
                                    }
                                    else{
                                        turnOffFlashlight(context)
                                    }
                                }
                            }
                        }
                    }

                }
                else if(messageBody.endsWith("LOCATION"))
                {
                    Toast.makeText(context, "Location", Toast.LENGTH_SHORT).show()
                    val securityCodeLength = messageBody.length - "LOCATION".length
                    val securityCode = messageBody.substring(0, securityCodeLength)
                    handleMessagesToggle(context, securityCode,messageBody,sender)
                }
                else if(messageBody.endsWith("CLOCATION2"))
                {

                    Toast.makeText(context, "CLOCATION2", Toast.LENGTH_SHORT).show()
                    (context as? RemoteMode)?.displayLocation(messageBody.toString())

                }

            }
        }
    }
    private fun getCurrentLocation(context: Context, Sender : String ) {


        // Create location manager
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Define location listener
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // Location update received
                currentLocation = location
                if (currentLocation != null) {
                    val latitude = currentLocation!!.latitude
                    val longitude = currentLocation!!.longitude

                    val message = "Current Location: \nLat=$latitude,\nLng=$longitude\nCLOCATION2"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    sendReplyMessage(context,Sender, message)
                } else {
                    Toast.makeText(context, "Failed to retrieve current location", Toast.LENGTH_SHORT).show()
                }

                // Remember to remove the location updates if you no longer need them
                locationManager?.removeUpdates(this)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {
                Toast.makeText(context, "GPS is disabled", Toast.LENGTH_SHORT).show()
            }

        }

        // Request location updates
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the missing permissions if needed.
            // See the documentation for ActivityCompat.requestPermissions for more details.
            return
        }
        locationManager?.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null)

    }

    private fun turnOnFlashlight(context: Context) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val cameraId = getCameraId(cameraManager)
                if (cameraId != null) {
                    cameraManager.setTorchMode(cameraId, true)
                } else {
                    Log.e("SMSReceiver", "Unable to find a camera with flash")
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
    private fun turnOffFlashlight(context: Context) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val cameraId = getCameraId(cameraManager)
                if (cameraId != null) {
                    cameraManager.setTorchMode(cameraId, false)
                } else {
                    Log.e("SMSReceiver", "Unable to find a camera with flash")
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun handleMessagesToggle(context: Context, securityCode: String, messageBody: String, sender:String) {
        CoroutineScope(Dispatchers.IO).launch {
            val appDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "my_db").build()
            val settingsDao = appDatabase.settingsDao()
            val settings = settingsDao.getSettingsById(1)

            withContext(Dispatchers.Main) {
                settings?.let {
                    if ((messageBody) == (it.securityCode+"FLASHON")) {
                        turnOnFlashlight(context)
                    }
                    else if((messageBody) == (it.securityCode+"FLASHOFF")) {
                        turnOffFlashlight(context)
                    }
                    else if((messageBody) == (it.securityCode+"SOUNDMAX")) {
                        //Toast.makeText(context, "calling soundmax method", Toast.LENGTH_SHORT).show()
                        turnOnSoundMax(context)
                    }
                    else if((messageBody) == (it.securityCode+"PLAYSOUND")) {
                        //Toast.makeText(context, "calling soundmax method", Toast.LENGTH_SHORT).show()
                        turnOnPLAYSOUND(context)
                    }
                    else if((messageBody) == (it.securityCode+"STOPSOUND")) {
                        //Toast.makeText(context, "calling soundmax method", Toast.LENGTH_SHORT).show()
                        turnOnSTOPSOUND(context)
                    }
                    else if((messageBody) == (it.securityCode+"LOCATION")) {
                        getCurrentLocation(context,sender);
                    }

                }
            }
        }
    }

    private fun checkStatus(context: Context, sender: String, securityCode: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val appDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "my_db").build()
            val settingsDao = appDatabase.settingsDao()
            val settings = settingsDao.getSettingsById(1)

            withContext(Dispatchers.Main) {
                settings?.let {
                    val replyMessage: String = if (securityCode == it.securityCode) {
                        val batteryPercentage = getBatteryPercentage(context)
                        val mobileDataEnabled = isMobileDataEnabled(context)
                        val gpsEnabled = isGpsEnabled(context)
                        val wifiEnabled = isWifiEnabled(context)
                        val soundMode = getSoundMode(context)
                        val ringingToneVolume = getRingingToneVolume(context)
                        val autoSyncEnabled = isAutoSyncEnabled(context)
                        val flashLight = isFlashLightEnabled(context)

                        Toast.makeText(context, "Flash Light : $flashLight", Toast.LENGTH_LONG).show()

                        "Battery : $batteryPercentage%\nMobile Data : $mobileDataEnabled\nGPS : $gpsEnabled\n" +
                                "WIFI : $wifiEnabled\nSound Mode : $soundMode\nRinging Tone Volume : $ringingToneVolume\n" +
                                "Auto Sync : $autoSyncEnabled\nFlash Light : $flashLight\nDONE"

                    } else {
                        "WRONGSECURITYCODE"
                    }
                    sendReplyMessage(context, sender, replyMessage)
                }
            }
        }
    }
    fun isFlashLightEnabled(context: Context): String {

        turnOffFlashlight(context)
        return "Off"

    }
    private fun turnOnSTOPSOUND(context: Context) {
            // Check if the mediaPlayer is currently playing
            // Stop the mediaPlayer if it's currently playing
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
                mediaPlayer?.reset()
            }

        // Stop all other playing sounds
        /*val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true)*/

        val receiver = SMSReceiver()

        // Unregister the existing receiver
        context.unregisterReceiver(this)

        // Register the receiver again
        val intentFilter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        context.registerReceiver(receiver, intentFilter)
    }
    private fun turnOnPLAYSOUND(context: Context) {
        //Toast.makeText(context, "sound = $sound", Toast.LENGTH_SHORT).show()
        // Check if the mediaPlayer is already playing
        if (mediaPlayer?.isPlaying == true) {
            // If already playing, stop it first before starting again
            Toast.makeText(context, " If already playing, stop it first before starting again", Toast.LENGTH_SHORT).show()
            mediaPlayer?.stop()
            mediaPlayer?.reset()
        } else {
            val ringtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            // Create a new instance of MediaPlayer
            mediaPlayer = MediaPlayer()

            // Set audio attributes for the MediaPlayer
            mediaPlayer?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )

            // Set the ringtone URI to the MediaPlayer
            mediaPlayer?.setDataSource(context, ringtoneUri)
            // Enable looping to play the ringtone continuously
            mediaPlayer?.isLooping = true
            // Prepare the MediaPlayer asynchronously
            mediaPlayer?.prepareAsync()

            // Start playing the system default ringtone
            mediaPlayer?.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.start()
            }
        }
    }
    private fun turnOnSoundMax(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Check sound mode
        val soundMode = audioManager.ringerMode

        // Change to sound mode if in silent or vibration mode
        if (soundMode == AudioManager.RINGER_MODE_SILENT || soundMode == AudioManager.RINGER_MODE_VIBRATE) {
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        }

        // Set all volume levels to max
        audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), 0)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION), 0)
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM), 0)

        Toast.makeText(context, "Sound Max", Toast.LENGTH_SHORT).show()
    }
    private fun getCameraId(cameraManager: CameraManager): String? {
        val cameraIds = cameraManager.cameraIdList
        for (id in cameraIds) {
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
            val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)

            if (hasFlash != null && hasFlash && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                return id
            }
        }
        return null
    }
    private fun sendReplyMessage(context: Context, sender: String, message: String) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(sender, null, message, null, null)
    }
    fun getBatteryPercentage(context: Context): Int {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }

        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        return if (level != -1 && scale != -1) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            -1
        }
    }
    fun isMobileDataEnabled(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mobileDataInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        return if (mobileDataInfo?.isConnected == true) "On" else "Off"
    }
    fun isGpsEnabled(context: Context): String {
        val locationMode: Int = try {
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
            return "Off"
        }

        return if (locationMode != Settings.Secure.LOCATION_MODE_OFF) "On" else "Off"
    }
    fun isWifiEnabled(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return if (wifiManager.isWifiEnabled) "On" else "Off"
    }
    fun getSoundMode(context: Context): String {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> "Silent"
            AudioManager.RINGER_MODE_VIBRATE -> "Vibrate"
            else -> "Normal"
        }
    }
    fun getRingingToneVolume(context: Context): Int {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.getStreamVolume(AudioManager.STREAM_RING)
    }
    fun isAutoSyncEnabled(context: Context): String {
        val contentResolver = context.contentResolver
        return if (ContentResolver.getMasterSyncAutomatically()) {
            "On"
        } else {
            "Off"
        }
    }



}
