package com.example.employeeexample.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface EmployeeListDao{
    @Query("SELECT * FROM employee ORDER BY name")
    fun getEmployees(): LiveData<List<Employee>>
}