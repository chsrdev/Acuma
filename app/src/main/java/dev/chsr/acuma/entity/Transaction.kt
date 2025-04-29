package dev.chsr.acuma.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: Int,
    @ColumnInfo("from_id") val fromId: Int?,
    @ColumnInfo("to_id") val toId: Int?,
    val amount: Int,
    val comment: String?,
    val date: Long,
)
