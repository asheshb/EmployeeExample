package com.example.employeeexample.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.employeeexample.R
import com.example.employeeexample.data.Employee
import kotlinx.android.synthetic.main.fragment_employee_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*

const val READ_FILE_REQUEST = 1
const val CREATE_FILE_REQUEST = 2
class EmployeeListFragment : Fragment() {

    private lateinit var viewModel: EmployeeListViewModel

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        viewModel = ViewModelProviders.of(this)
            .get(EmployeeListViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_employee_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        with(employee_list){
            layoutManager = LinearLayoutManager(activity)
            adapter = EmployeeAdapter {
                findNavController().navigate(
                    EmployeeListFragmentDirections.actionEmployeeListFragmentToEmployeeDetailFragment(
                        it
                    )
                )
            }
        }

        add_employee.setOnClickListener{
            findNavController().navigate(
                EmployeeListFragmentDirections.actionEmployeeListFragmentToEmployeeDetailFragment(
                    0
                )
            )
        }

        viewModel.employees.observe(viewLifecycleOwner, Observer {
            (employee_list.adapter as EmployeeAdapter).submitList(it)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_menu, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_import_data -> {
                importEmployees()
                true
            }
            R.id.menu_export_data -> {
                exportEmployees()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                READ_FILE_REQUEST -> {
                    data?.data?.also { uri ->
                        GlobalScope.launch {
                            readFromFile(uri)
                        }
                    }
                }
                CREATE_FILE_REQUEST -> {
                    data?.data?.also { uri ->
                        GlobalScope.launch {
                            writeToFile(uri)
                        }
                    }
                }
            }
        }
    }

    private fun exportEmployees(){
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "employee_list.csv")
        }
        startActivityForResult(intent, CREATE_FILE_REQUEST)
    }

    private suspend fun writeToFile(uri: Uri){
        try {
            activity!!.applicationContext.contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use {out ->
                    val employees = viewModel.getEmployeeList()
                    if(employees.isNotEmpty()){
                        employees.forEach{
                            out.write((it.name + "," + it.role + "," + it.age + "," + it.gender).toByteArray())
                        }
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun importEmployees(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"

        }
        startActivityForResult(intent, READ_FILE_REQUEST)
    }

    private suspend fun readFromFile(uri: Uri){

        try {
            activity!!.applicationContext.contentResolver.openFileDescriptor(uri, "r")?.use {
                FileInputStream(it.fileDescriptor).use {
                    withContext(Dispatchers.IO) {
                        parseCSVFile(it)
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private suspend fun parseCSVFile(stream: FileInputStream){
        val employees = mutableListOf<Employee>()
        BufferedReader(InputStreamReader(stream)).forEachLine {
            val tokens = it.split(",")
            employees.add(Employee(id = 0, name = tokens[0], role = tokens[1].toInt(),
                age = tokens[2].toInt(), gender = tokens[3].toInt(), photo = ""))
        }

        if(employees.isNotEmpty()){
            viewModel.insertEmployees(employees)
        }
    }

}
