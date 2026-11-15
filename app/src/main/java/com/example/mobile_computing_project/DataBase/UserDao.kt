package com.example.mobile_computing_project.DataBase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM User WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): UserEntity?

    @Query("SELECT * FROM User WHERE phone = :phone LIMIT 1")
    suspend fun getByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM User WHERE userId = :id")
    suspend fun getById(id: Int): UserEntity?

    @Query("UPDATE User SET balance = :balance WHERE userId = :id")
    suspend fun updateBalance(id: Int, balance: Long)
}