package com.example.employeeexample.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.employeeexample.data.Employee
import com.example.employeeexample.data.EmployeeDetailRepository
import kotlinx.coroutines.launch

class EmployeeDetailViewModel(application: Application): AndroidViewModel(application){
    private val repo: EmployeeDetailRepository =
        EmployeeDetailRepository(application)

    private val _employeeId = MutableLiveData<Long>(0)
    val employeeId: LiveData<Long>
        get() = _employeeId

    val employee: LiveData<Employee> = Transformations
        .switchMap(_employeeId) { id ->
            repo.getEmployee(id)
        }

    fun setEmployeeId(id: Long){
        if(_employeeId.value != id ) {
            _employeeId.value = id
        }
    }

    fun saveEmployee(employee: Employee){
        viewModelScope.launch {
            if (_employeeId.value == 0L) {
                _employeeId.value = repo.insertEmployee(employee)
            } else {
                repo.updateEmployee(employee)
            }
        }
    }

    fun deleteEmployee(){
        viewModelScope.launch {
            employee.value?.let { repo.deleteEmployee(it) }
        }
    }
}