package com.example.employeeexample.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Employee::class], version = 1)
abstract class EmployeeDatabase : RoomDatabase() {

    abstract fun employeeListDao(): EmployeeListDao
    abstract fun employeeDetailDao(): EmployeeDetailDao

    companion object {
        @Volatile
        private var instance: EmployeeDatabase? = null

        fun getDatabase(context: Context) = instance
            ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                EmployeeDatabase::class.java,
                "employee_database"
            ).build().also { instance = it }
        }
    }
}