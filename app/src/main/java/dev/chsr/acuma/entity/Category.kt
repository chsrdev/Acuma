package dev.chsr.acuma.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val percent: Int = 0,
    val balance: Int = 0,
    val goal: Int?
)
