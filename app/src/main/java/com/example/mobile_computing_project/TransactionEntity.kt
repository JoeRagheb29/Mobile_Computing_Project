package com.example.mobile_computing_project

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["userId"], childColumns = ["senderId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = UserEntity::class, parentColumns = ["userId"], childColumns = ["receiverId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val transactionId: Int = 0,
    val senderId: Int,
    val receiverId: Int,
    val amount: Long,
    val timestamp: Long = System.currentTimeMillis()
)