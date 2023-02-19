package com.example.happyplaces

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.happyplaces.databinding.ActivityAddHappyPlaceBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class AddHappyPlace : AppCompatActivity(), View.OnClickListener {
    // Request ID not used since permission request are handled by the Dexter library
    companion object {
        private const val GALLERY_ACTION_ID = 1;
        private const val CAMERA_ACTION_ID = 2;
        // Folder on the phone where the image is stored
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
    }


    private var binding: ActivityAddHappyPlaceBinding? = null

    val calendar = Calendar.getInstance()
    lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        // Adding back button
        setSupportActionBar(binding?.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.toolbarAddPlace?.setNavigationOnClickListener {
            onBackPressed()
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        //Make sure the edit text uses the onClickListener
        binding?.etDate?.setOnClickListener(this)
        binding?.tvAddImage?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.et_date -> {
                DatePickerDialog(
                    this@AddHappyPlace,
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            R.id.tv_add_image -> {
                val pictureDialog = AlertDialog.Builder(this@AddHappyPlace)
                pictureDialog.setTitle("Select action")
                val pictureDialogItems =
                    arrayOf("Select photo from gallery ", " Capture image with camera")
                pictureDialog.setItems(pictureDialogItems) {
                    // Dialog that was selected and item index
                        _, item ->
                    when (item) {
                        // First item in array
                        0 -> {
                            choosePhotoFromGallery()
                        }
                        1 -> {
                            takePhotoFromCamera()
                        }
                    }
                }
                // Remember to be careful with code placements if there are a lot of brackets
                pictureDialog.show()
            }
        }
    }

    private fun choosePhotoFromGallery() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                // All necessary permissions have been granted
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY_ACTION_ID)
                }
            }

            // MutableList used so that we can change the number of permissions later on
            // This method shows the user why the permission is needed in case the user rejected the proposed permissions
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest?>?,
                token: PermissionToken?
            ) {
                showRationaleDialogForPermissions()
            }
        }).onSameThread().check()
    }

    private fun takePhotoFromCamera() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                // All necessary permissions have been granted
                if (report!!.areAllPermissionsGranted()) {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, CAMERA_ACTION_ID)
                }
            }

            // MutableList used so that we can change the number of permissions later on
            // This method shows the user why the permission is needed in case the user rejected the proposed permissions
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest?>?,
                token: PermissionToken?
            ) {
                showRationaleDialogForPermissions()
            }
        }).onSameThread().check()
    }

    // Run if the gallery functionality is accessed in order to get the result of the gallery functionality
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_ACTION_ID) {
                // Check if the data returned is not null
                if (data != null) {
                    val contentURI = data.data
                    // Get image from the MediaStore
                    try {
                        val selectedImageBitMap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        // Image stored and location returned
                        val fileLocation = saveImageToInternalStorage(selectedImageBitMap)
                        Log.e("File location ", "Path : $fileLocation")
                        binding?.ivPlaceImage?.setImageBitmap(selectedImageBitMap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@AddHappyPlace,
                            "Failed to load image from gallery",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else if (requestCode == CAMERA_ACTION_ID) {
                val cameraBitMap: Bitmap? = data?.extras?.get("data") as Bitmap?
                // Image stored and location returned
                val fileLocation = saveImageToInternalStorage(cameraBitMap!!)
                Log.e("File location " , "Path : $fileLocation " )
                binding?.ivPlaceImage?.setImageBitmap(cameraBitMap)
            }
        }
    }

    private fun showRationaleDialogForPermissions() {
        AlertDialog.Builder(this@AddHappyPlace)
            .setMessage("It looks like you have rejected the permissions required for this feature." +
                "It can be enabled under applications settings")
            .setPositiveButton("GO TO SETTINGS")  {
                _,_ ->
                    try {
                        // Use intents to send user directly to permission settings
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        // We pass the package name to be used as an identifier of the application that wants to alter its permissions
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                    }
            }
            .setNegativeButton("Cancel") {
                dialog,_ ->
                    dialog.dismiss()
            } .show()
    }

    private fun updateDateInView() {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding?.etDate?.setText(sdf.format(calendar.time).toString())
    }

    // Return the location of where the image is stored
    private fun saveImageToInternalStorage(bitMap: Bitmap) : Uri {
        // We need it to get the directory of our application
        val wrapper = ContextWrapper(applicationContext)
        // The mode we set makes this directory accessible only from this application
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        // Create the file where the image is stored. The UUID gives the file name a random UUID so that each image
        // will have a unique file naem
        file = File(file,"${UUID.randomUUID()}.jpeg")
        try{
            val stream : OutputStream = FileOutputStream(file)
            bitMap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch(e : IOException) {
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }
}