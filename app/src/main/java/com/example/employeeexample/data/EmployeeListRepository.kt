package com.example.employeeexample.data

import android.app.Application
import androidx.lifecycle.LiveData


class EmployeeListRepository(context: Application){
    private val employeeListDao: EmployeeListDao =
        EmployeeDatabase.getDatabase(context).employeeListDao()

    fun getEmployees(): LiveData<List<Employee>> =
        employeeListDao.getEmployees()

}