package com.example.findphone

import com.example.findphone.SMSReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony
import android.view.MenuItem
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.room.Room
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest
import android.annotation.SuppressLint
import android.widget.Switch

class MainActivity : AppCompatActivity() {




    private lateinit var navHeaderUserName: TextView
    private lateinit var navHeaderGmailAddress: TextView

    lateinit var toggle : ActionBarDrawerToggle
    private lateinit var settingsDao: SettingsDao

    companion object {
        private const val REQUEST_SMS_PERMISSION = 123
        private const val REQUEST_LOCATION_PERMISSION = 124
    }




    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request location updates
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the missing permissions if needed.
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
            // See the documentation for ActivityCompat.requestPermissions for more details.
            return
        }




        val navViewMain: NavigationView = findViewById(R.id.nav_view)

        // Get the header layout from the NavigationView
        val headerView = navViewMain.getHeaderView(0)

        // Find the user_name TextView within the header layout
        navHeaderUserName = headerView.findViewById(R.id.user_name)
        navHeaderGmailAddress = headerView.findViewById(R.id.email_address)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT


        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayout)
        val navView : NavigationView = findViewById(R.id.nav_view)
        val btnFind = findViewById<TextView>(R.id.btn_find)

        val appDatabase = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "my_db").build()
        settingsDao = appDatabase.settingsDao()

        CoroutineScope(Dispatchers.IO).launch {
            val settings = settingsDao.getSettingsById(1)
            withContext(Dispatchers.Main) {
                settings?.let {
                    navHeaderUserName.text = it.userName
                    navHeaderGmailAddress.text = it.gMail
                } ?: run {
                    navHeaderUserName.text = "User Name"
                    navHeaderGmailAddress.text = "Example@gmsil.com"

                }
            }
        }


        toggle = ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.setting -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
                R.id.home -> recreate() // Refresh the current activity (MainActivity)
            }
            true
        }
        btnFind.setOnClickListener {

            val intent = Intent(this,FindActivity::class.java)
            startActivity(intent)
        }

        // Check if the permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS), REQUEST_SMS_PERMISSION)
        } else {
            // Permission is already granted, proceed with SMS receiving logic
            enableSmsReceiver()
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_SMS_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with SMS receiving logic
                    enableSmsReceiver()
                } else {
                    // Permission denied, handle accordingly (e.g., show an explanation, disable SMS functionality, etc.)
                }
            }
        }
    }
    private fun enableSmsReceiver() {
        val smsReceiver = SMSReceiver()
        val intentFilter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        registerReceiver(smsReceiver, intentFilter)
    }
}