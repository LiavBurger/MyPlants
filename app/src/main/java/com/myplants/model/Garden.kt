package com.myplants.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gardens")
data class Garden(
    @PrimaryKey val userId: String,
    val ownerName: String,
    val ownerImageUrl: String
)
