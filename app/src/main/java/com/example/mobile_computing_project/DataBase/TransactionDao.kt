package com.example.mobile_computing_project.DataBase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Query("""SELECT * FROM transactions
              WHERE senderId = :userId OR receiverId = :userId
              ORDER BY timestamp DESC""")
    suspend fun getForUser(userId: Int): List<TransactionEntity>
}