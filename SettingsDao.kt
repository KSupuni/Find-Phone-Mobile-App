// SettingsDao.kt
package com.example.findphone

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SettingsDao {

    @Insert
    suspend fun insertSettings(settings: TableSettings)

    @Update
    suspend fun updateSettings(settings: TableSettings)

    @Query("SELECT * FROM TableSettings WHERE ID = :id")
    suspend fun getSettingsById(id: Int): TableSettings?
}
