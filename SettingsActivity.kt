package com.example.findphone

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.timer

class SettingsActivity : AppCompatActivity() {

    private lateinit var btnSave: Button
    private lateinit var etGmailAddress: EditText
    private lateinit var etName: EditText
    private lateinit var etSecurityCode: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var etvSecurityCode: EditText
    private lateinit var settingsDao: SettingsDao

    private lateinit var initialSecurityCode: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        supportActionBar?.hide()

        btnSave = findViewById(R.id.btnSave)
        etGmailAddress = findViewById(R.id.etGmailAddress)
        etName = findViewById(R.id.etName)
        etSecurityCode = findViewById(R.id.etSecurityCode)
        etvSecurityCode = findViewById(R.id.etvSecurityCode)
        etPhoneNumber = findViewById(R.id.etbPhoneNumber)

        val appDatabase = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "my_db").build()
        settingsDao = appDatabase.settingsDao()


        CoroutineScope(Dispatchers.IO).launch {
            val settings = settingsDao.getSettingsById(1)
            withContext(Dispatchers.Main) {
                settings?.let {
                    etGmailAddress.setText(it.gMail)
                    etName.setText(it.userName)
                    etSecurityCode.setText(it.securityCode)
                    etPhoneNumber.setText(it.phoneNumber)
                    // Store the initial security code value
                    initialSecurityCode = it.securityCode
                } ?: run {
                    // Table is empty, set default values
                    etGmailAddress.setText("Example@gmail.com")
                    etName.setText("User Name")
                    etSecurityCode.setText("XXXX")
                    etPhoneNumber.setText("07XXXXXXXX")
                    initialSecurityCode = "XXXX"
                }
            }
        }

        btnSave.setOnClickListener {

            val scode = etSecurityCode.text.toString()
            val vscode = etvSecurityCode.text.toString()




            if (scode==initialSecurityCode && vscode.isEmpty())
            {
                save()
            }
            else if(scode==vscode)
            {
                save()
            }
            else{
                Toast.makeText(this, "Security codes are not matching", Toast.LENGTH_SHORT).show()
            }


        }
    }
    private fun save(){

        val gmailAddress = etGmailAddress.text.toString()
        val name = etName.text.toString()
        val securityCode = etSecurityCode.text.toString()
        val phoneNumber = etPhoneNumber.text.toString()

        val settings = TableSettings(ID = 1, gMail = gmailAddress, userName = name, securityCode = securityCode, phoneNumber = phoneNumber)


        updateOrInsertSettings(settings)
        Toast.makeText(this, "All details saved.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun updateOrInsertSettings(settings: TableSettings) {
        CoroutineScope(Dispatchers.IO).launch {
            val existingSettings = settingsDao.getSettingsById(1)
            if (existingSettings != null) {
                // Update existing settings
                settingsDao.updateSettings(settings)
            } else {
                // Insert new settings
                settingsDao.insertSettings(settings)
            }
        }
    }



}