package com.example.mobile_computing_project.DataBase

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "User", indices = [
    Index(value = ["Username"], unique = true),
    Index(value = ["Phone"], unique = true),
    Index(value = ["Password"], unique = true)
])
data class UserEntity (
    @PrimaryKey(autoGenerate = true)
        val UserId: Int=0,
        val Username: String,
        val Password: String,
        val Phone: String,
        val Balance: Long=0
)