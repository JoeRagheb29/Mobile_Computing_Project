package com.example.mobile_computing_project.DataBase

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class NotificationType {
    TRANSFER_RECEIVED,
    TRANSFER_SENT,
    TRANSFER_FAILED,
    SECURITY_ALERT,
    GENERAL
}

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val remoteId: String? = null, // لو الـ Backend بيبعت ID مميز للإشعار لتجنب التكرار
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.GENERAL,
    val transactionId: String? = null
)

class NotificationConverters {
    @TypeConverter
    fun fromType(type: NotificationType): String = type.name

    @TypeConverter
    fun toType(value: String): NotificationType =
        runCatching { NotificationType.valueOf(value) }.getOrDefault(NotificationType.GENERAL)
}