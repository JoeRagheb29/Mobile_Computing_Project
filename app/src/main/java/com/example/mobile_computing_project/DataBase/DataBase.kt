package com.example.mobile_computing_project.DataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [UserEntity::class, TransactionEntity::class, NotificationEntity::class], version = 2)
@TypeConverters(
    NotificationConverters::class
)
abstract class Daf3aDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun notificationDao(): NotificationDao
    companion object {
        @Volatile
        private var INSTANCE: Daf3aDatabase? = null

        fun getInstance(context: Context): Daf3aDatabase {
            if (INSTANCE == null) {
                synchronized(Daf3aDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        Daf3aDatabase::class.java,
                        "Daf3a_db"
                    )
                        .fallbackToDestructiveMigration(true)
                        .build()
                }
            }
            return INSTANCE!!
        }
    }
}