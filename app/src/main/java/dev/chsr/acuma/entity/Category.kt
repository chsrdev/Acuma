package dev.chsr.acuma.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: Int,
    val name: String,
    val percent: Float = 0f,
    val balance: Int = 0,
)
