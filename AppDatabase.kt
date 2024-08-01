// AppDatabase.kt
package com.example.findphone

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TableSettings::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao


}