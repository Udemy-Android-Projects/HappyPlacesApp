package com.example.happyplaces.activities

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
import com.example.happyplaces.R
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.databinding.ActivityAddHappyPlaceBinding
import com.example.happyplaces.models.HappyPlaceModel
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
    // We are getting the reference of the image we will store as a global variable
    private var saveImageToInternalStorage: Uri? = null
    // We will also store the longitude and latitude using global variables
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

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
        // We want to give the user the date automatically by calling this method outside the dateListener
        updateDateInView()

        //Make sure the edit text uses the onClickListener
        binding?.etDate?.setOnClickListener(this)
        binding?.tvAddImage?.setOnClickListener(this)
        binding?.btnSave?.setOnClickListener(this)
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
            R.id.btn_save -> {
                // We have to use a when expression which is a version of a switch statement
                when {
                    // We have to check if there are values that have been entered since we don't want to store empty values
                    binding?.etTitle?.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
                    }
                    binding?.etDescription?.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT)
                            .show()
                    }
                    binding?.etLocation?.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please select location", Toast.LENGTH_SHORT)
                            .show()
                    }
                    // We don't check if the date is empty since the method that creates the date is in the onCreate method
                    // and hence its created once onCreate is called

                    // If the reference to the image is null we inform the user to select an image
                    saveImageToInternalStorage == null -> {
                        Toast.makeText(this, "Please add image", Toast.LENGTH_SHORT).show()
                    } else -> { // If everything is okay store the data collected
                        // Create a happyPlace object using the information the user has keyed in
                    val happyPlaceModel = HappyPlaceModel(
                        0,// ID is zero since it will be auto incremented
                        binding?.etTitle?.text.toString(),
                        saveImageToInternalStorage.toString(),
                        binding?.etDescription?.text.toString(),
                        binding?.etDate?.text.toString(),
                        binding?.etLocation?.text.toString(),
                        mLatitude,
                        mLongitude
                    )
                    // We now store the data
                    // Here we initialize the database handler class.
                    val dbHandler = DatabaseHandler(this)
                    // Remember the addHappyPlace function returns a long that indicates the result of the operation
                    val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)
                    if (addHappyPlace > 0) {
                        // Confirms that data has been successfully added to the database
                        // We have to use this method since data entry and data reading occur in different classes
                        setResult(Activity.RESULT_OK)
                        Toast.makeText(
                            this,
                            "The happy place details are inserted successfully.",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Once we finish this activity we will be sent back to the main activity
                        finish()
                    }
                    }
                }
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
                        // The variable context is global
                        saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitMap)
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
                // the variable context is now global
                saveImageToInternalStorage = saveImageToInternalStorage(cameraBitMap!!)
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