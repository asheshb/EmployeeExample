package com.example.employeeexample.ui


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.employeeexample.R
import com.example.employeeexample.data.Employee
import com.example.employeeexample.data.Gender
import com.example.employeeexample.data.Role


import kotlinx.android.synthetic.main.fragment_employee_detail.*
import kotlinx.android.synthetic.main.fragment_employee_detail.employee_photo


/**
 * A simple [Fragment] subclass.
 */
class EmployeeDetailFragment : Fragment() {

    private lateinit var viewModel: EmployeeDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this)
            .get(EmployeeDetailViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_employee_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val roles = mutableListOf<String>()
        Role.values().forEach { roles.add(it.name)}
        val arrayAdapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, roles)
        employee_role.adapter = arrayAdapter

        val ages = mutableListOf<Int>()
        for(i in 18 until 81){ ages.add(i) }
        employee_age.adapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, ages)

        val id = EmployeeDetailFragmentArgs.fromBundle(requireArguments()).id
        viewModel.setEmployeeId(id)

        viewModel.employee.observe(viewLifecycleOwner, Observer {
            it?.let{ setData(it) }
        })

        save_employee.setOnClickListener {
            saveEmployee()
        }

        delete_employee.setOnClickListener {
            deleteEmployee()
        }
    }

    private fun setData(employee: Employee){
        employee_name.setText(employee.name)

        employee_role.setSelection(employee.role)
        employee_age.setSelection(employee.age - 18)

        when (employee.gender) {
            Gender.Male.ordinal -> {
                gender_male.isChecked = true
            }
            Gender.Female.ordinal -> {
                gender_female.isChecked = true
            }
            else -> {
                gender_other.isChecked = true
            }
        }
    }

    private fun saveEmployee(){
        val name = employee_name.text.toString()
        val role = employee_role.selectedItemPosition
        val age = employee_age.selectedItemPosition + 18

        val selectedStatusButton =  gender_group.findViewById<RadioButton>(gender_group.checkedRadioButtonId)
        var gender = Gender.Other.ordinal
        if(selectedStatusButton.text == Gender.Male.name){
            gender = Gender.Male.ordinal
        } else if(selectedStatusButton.text == Gender.Female.name) {
            gender = Gender.Female.ordinal
        }
        val employee = Employee(viewModel.employeeId.value!!, name, role, age, gender, "")
        viewModel.saveEmployee(employee)

        activity!!.onBackPressed()
    }

    private fun deleteEmployee(){
        viewModel.deleteEmployee()

        activity!!.onBackPressed()
    }
}
