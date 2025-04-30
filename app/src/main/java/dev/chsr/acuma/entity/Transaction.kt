package dev.chsr.acuma.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo("from_id") val fromId: Int?,
    @ColumnInfo("to_id") val toId: Int?,
    val amount: Int,
    val comment: String?,
    val date: Long,
)
