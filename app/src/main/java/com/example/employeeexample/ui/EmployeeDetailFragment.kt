package com.example.employeeexample.ui


import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.employeeexample.BuildConfig
import com.example.employeeexample.R
import com.example.employeeexample.data.Employee
import com.example.employeeexample.data.Gender
import com.example.employeeexample.data.Role
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_employee_detail.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


const val PERMISSION_REQUEST_CAMERA = 0
const val CAMERA_PHOTO_REQUEST = 1
const val GALLERY_PHOTO_REQUEST = 2

class EmployeeDetailFragment : Fragment() {

    private lateinit var viewModel: EmployeeDetailViewModel
    //For simplicity this variable is here but should be moved to ViewModel
    private var selectedPhotoPath: String = ""

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

        //reset image
        employee_photo.setOnClickListener{
            employee_photo.setImageResource(R.drawable.blank_photo)
            employee_photo.tag = ""
        }

        photo_from_camera.setOnClickListener{
            clickPhotoAfterPermission(it)
        }

        photo_from_gallery.setOnClickListener{
            pickPhoto()
        }
    }

    private fun setData(employee: Employee){
        with(employee.photo){
            if(isNotEmpty()){
                employee_photo.setImageURI(Uri.parse(this))
                employee_photo.tag = this

            } else{
                employee_photo.setImageResource(R.drawable.blank_photo)
                employee_photo.tag = ""
            }
        }

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
        val photo = employee_photo.tag as String

        val employee = Employee(viewModel.employeeId.value!!, name, role, age, gender, photo)
        viewModel.saveEmployee(employee)

        activity!!.onBackPressed()
    }

    private fun deleteEmployee(){
        viewModel.deleteEmployee()

        activity!!.onBackPressed()
    }

    private fun clickPhotoAfterPermission(view: View){
        if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED) {
            clickPhoto()
        } else {
            requestCameraPermission(view)
        }
    }

    private fun requestCameraPermission(view: View) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.CAMERA)) {
            val snack = Snackbar.make(view, getString(R.string.camera_permission), Snackbar.LENGTH_INDEFINITE)
            snack.setAction("OK", View.OnClickListener {
                requestPermissions(arrayOf(Manifest.permission.CAMERA),
                    PERMISSION_REQUEST_CAMERA)
            })
            snack.show()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CAMERA)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                clickPhoto()
            } else {
                Toast.makeText(activity!!, getString(R.string.camera_permission_denied),
                    Toast.LENGTH_SHORT). show()
            }
        }
    }

    private fun clickPhoto(){
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(activity!!.packageManager)?.also {
                val photoFile: File? = try {
                    createFile(activity!!, Environment.DIRECTORY_PICTURES, "jpg")
                } catch (ex: IOException) {
                    Toast.makeText(activity!!, getString(R.string.camera_file_Error, ex.message),
                        Toast.LENGTH_SHORT).show()
                    null
                }
                photoFile?.also {
                    selectedPhotoPath = it.absolutePath
                    val photoURI: Uri = FileProvider.getUriForFile(
                        activity!!,
                        BuildConfig.APPLICATION_ID + ".fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, CAMERA_PHOTO_REQUEST)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == RESULT_OK){
            when(requestCode){
                CAMERA_PHOTO_REQUEST -> {
                    val uri = Uri.fromFile(File(selectedPhotoPath))
                    employee_photo.setImageURI(uri)
                    employee_photo.tag = uri.toString()
                }
                GALLERY_PHOTO_REQUEST ->{
                    data?.data?.also { uri ->
                        val photoFile: File? = try {
                            createFile(activity!!, Environment.DIRECTORY_PICTURES, "jpg")
                        } catch (ex: IOException) {
                            Toast.makeText(activity!!, getString(R.string.image_file_Error, ex.message),
                                Toast.LENGTH_SHORT). show()
                            null
                        }
                        photoFile?.also {
                            try {
                                val resolver = activity!!.applicationContext.contentResolver
                                resolver.openInputStream(uri).use { stream ->
                                    val output = FileOutputStream(photoFile)
                                    stream!!.copyTo(output)
                                }
                                val fileUri = Uri.fromFile(photoFile)
                                employee_photo.setImageURI(fileUri)
                                employee_photo.tag = fileUri.toString()
                            } catch (e: FileNotFoundException) {
                                e.printStackTrace()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun pickPhoto(){
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickPhotoIntent.resolveActivity(activity!!.packageManager)?.also {
            startActivityForResult(pickPhotoIntent, GALLERY_PHOTO_REQUEST)
        }


    }
}
