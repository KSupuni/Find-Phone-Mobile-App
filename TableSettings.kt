package com.example.findphone

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
data class TableSettings(
    @PrimaryKey(autoGenerate = true)
    val ID:Int=0,
    val userName: String,
    val gMail: String,
    val phoneNumber: String,
    val securityCode:String

)