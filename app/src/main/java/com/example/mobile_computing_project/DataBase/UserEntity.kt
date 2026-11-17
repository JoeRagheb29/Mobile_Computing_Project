package com.example.mobile_computing_project.DataBase

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "User", indices = [
    Index(value = ["username"], unique = true),
    Index(value = ["phone"], unique = true)
])
data class UserEntity (
    @PrimaryKey(autoGenerate = true)
        val userId: Int=0,
        val username: String,
        val password: String,
        val phone: String,
        val balance: Long=0
)