package com.example.employeeexample.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.employeeexample.R
import com.example.employeeexample.data.Employee
import com.example.employeeexample.data.Gender
import com.example.employeeexample.data.Role
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_item.*
import kotlinx.android.synthetic.main.list_item.employee_age
import kotlinx.android.synthetic.main.list_item.employee_name
import kotlinx.android.synthetic.main.list_item.employee_photo
import kotlinx.android.synthetic.main.list_item.employee_role


class EmployeeAdapter(private val listener: (Long) -> Unit):
    ListAdapter<Employee, EmployeeAdapter.ViewHolder>(
        DiffCallback()
    ){

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        val itemLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)

        return ViewHolder(itemLayout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder (override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {
        init{
            itemView.setOnClickListener{
                listener.invoke(getItem(adapterPosition).id)
            }
        }

        fun bind(employee: Employee){
            with(employee) {

                employee_name.text = name
                employee_role.text =  Role.values()[employee.role].name
                employee_age.text = containerView.context.resources
                    .getString(R.string.years, employee.age)

                employee_gender.text = Gender.values()[employee.gender].name

                with(photo){
                    if(isNotEmpty()){
                        employee_photo.setImageURI(Uri.parse(this))
                    } else{
                        employee_photo.setImageResource(R.drawable.blank_photo)
                    }
                }
            }
        }
    }
}

class DiffCallback : DiffUtil.ItemCallback<Employee>() {
    override fun areItemsTheSame(oldItem: Employee, newItem: Employee): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Employee, newItem: Employee): Boolean {
        return oldItem == newItem
    }
}