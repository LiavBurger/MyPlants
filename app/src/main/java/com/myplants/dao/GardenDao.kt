package com.myplants.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.myplants.model.Garden

@Dao
interface GardenDao {
    @Query("SELECT * FROM gardens")
    suspend fun getAll(): List<Garden>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(gardens: List<Garden>)

    @Query("DELETE FROM gardens")
    suspend fun deleteAll()
}
