package com.example.happyplaces.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
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
import com.example.happyplaces.utils.GetAddressFromLatLng
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteFragment
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
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
        // TODO Using the google places API (Step 4: Create a constant variable for place picker)
        // START
        // A constant variable for place picker
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
        // END
    }

    private var binding: ActivityAddHappyPlaceBinding? = null

    val calendar = Calendar.getInstance()
    lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    // We are getting the reference of the image we will store as a global variable
    private var saveImageToInternalStorage: Uri? = null
    // We will also store the longitude and latitude using global variables
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    // This is the variable that will be populated by the values from the object sent using notifyEditItem
    private var mHappyPlaceDetails : HappyPlaceModel? = null

    // TODO Getting The Users Location(Step 2: Add a variable for FusedLocationProviderClient which is later used to get the current location.)
    private lateinit var mFusedLocationClient: FusedLocationProviderClient // A fused location client variable which is further user to get the user's current location


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

        // TODO Using the google places API (Step 2: Initialize the places sdk if it is not initialized earlier.)
        // START
        /**
         * Initialize the places sdk if it is not initialized earlier using the api key.
         */
        if(!Places.isInitialized()) {
            // Takes as parameters activity and the key provided by google
            Places.initialize(
                this@AddHappyPlace,
                resources.getString(R.string.google_api_key)
            )
        }
        // END step 2

        // TODO Getting The Users Location (Step 3: Initialize the Fused location variable)
        // START
        // Initialize the Fused location variable
        // Can also be initialized as a local variable in a method
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // END

        // TODO (Step 7: Assign the details to the variable of data model class which we have created above the details which we will receive through intent.)
        // START
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mHappyPlaceDetails =
                intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }
        // END

        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        // We want to give the user the date automatically by calling this method outside the dateListener
        updateDateInView()

        // TODO (Step 8: Filling the existing details to the UI components to edit.)
        // START
        // If not null we know we are editing
        if (mHappyPlaceDetails != null) {
            supportActionBar?.title = "Edit Happy Place"

            binding?.etTitle?.setText(mHappyPlaceDetails!!.title)
            binding?.etDescription?.setText(mHappyPlaceDetails!!.description)
            binding?.etDate?.setText(mHappyPlaceDetails!!.date)
            binding?.etLocation?.setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitude
            mLongitude = mHappyPlaceDetails!!.longitude

            // Getting image location
            saveImageToInternalStorage = Uri.parse(mHappyPlaceDetails!!.image)

            binding?.ivPlaceImage?.setImageURI(saveImageToInternalStorage)

            binding?.btnSave?.text = "UPDATE"
        }
        // END

        //Make sure the edit text uses the onClickListener
        binding?.etDate?.setOnClickListener(this)
        binding?.tvAddImage?.setOnClickListener(this)
        binding?.btnSave?.setOnClickListener(this)
        // TODO Using the google places API (Step 3: Set a click Listener to location edit text after making it focusable false.)
        // START
        binding?.etLocation?.setOnClickListener(this)
        // END

        // TODO Adding the select current location button and setting permissions (Step 2: Assign a click listener to the select current location textview.)
        // START
        binding?.tvSelectCurrentLocation?.setOnClickListener(this)
        // END
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
                    }
                    else -> { // If everything is okay store the data collected
                    // Create a happyPlace object using the information the user has keyed in
                    val happyPlaceModel = HappyPlaceModel(
                        // TODO Fixing Update Bug (Step 2: Changing the id if it is for edit.)
                        // START
                        if (mHappyPlaceDetails == null) 0 else mHappyPlaceDetails!!.id,
                        // END
                        // ID is zero since it will be auto incremented
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
                    // TODO Fixing Update Bug(Step 3: Call add or update details conditionally.)
                    // START
                    if (mHappyPlaceDetails == null) {
                        val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)
                        // Remember the addHappyPlace function returns a long that indicates the result of the operation
                        if (addHappyPlace > 0) {
                            // Confirms that data has been successfully added to the database
                            // We have to use this method since data entry and data reading occur in different classes
                            setResult(Activity.RESULT_OK);
                            Toast.makeText(
                                this,
                                "The happy place details are inserted successfully.",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()//finishing activity
                        }
                    } else {
                        val updateHappyPlace = dbHandler.updateHappyPlace(happyPlaceModel)

                        if (updateHappyPlace > 0) {
                            setResult(Activity.RESULT_OK);
                            Toast.makeText(
                                this,
                                "The happy place details have been updated successfully.",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()//finishing activity
                        }
                    }
                    // END
                }
                }
            }
            // TODO Using the google places API (Step 5: Add an onClick event on the location for place picker)
            // START
            R.id.et_location -> {
                try {
                // These are the list of fields which we required is passed
                val fields = listOf(
                    Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                    Place.Field.ADDRESS
                )
                // Start the autocomplete intent with a unique request code.
                    // It will be an activity that pops in front of ours that was created by google
                val intent =
                    Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(this@AddHappyPlace)
//                    AutocompleteFragment.
//                    placeAutoComplete.getView().setBackgroundColor(Color.WHITE)
                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            // END
            // TODO Adding the select current location button and setting permissions (Step 5: Add a click event for selecting the current location)
            // START
            R.id.tv_select_current_location -> {
                // Ask user initially if they want to grant permissions...initial inquiry
                if (!isLocationEnabled()) {
                    Toast.makeText(
                        this,
                        "Your location provider is turned off. Please turn it on.",
                        Toast.LENGTH_SHORT
                    ).show()
                    // This will redirect you to settings from where you need to turn on the location provider.
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else
                // At this point Dexter has been used as a follow up service to ensure that the user can either deny or accept permissions...follow up inquiry
                {
                    // For Getting current location of user please have a look at below link for better understanding
                    // https://www.androdocs.com/kotlin/getting-current-location-latitude-longitude-in-android-using-kotlin.html
                    Dexter.withActivity(this)
                        .withPermissions(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        .withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                if (report!!.areAllPermissionsGranted()) {
                                    // TODO Getting The Users Location(Step 6: Remove the toast message and Call the new request location function to get the latest location.)
                                    // START
                                    requestNewLocationData()
                                    // END
                                }
                            }
                            // User didn't give permissions
                            override fun onPermissionRationaleShouldBeShown(
                                permissions: MutableList<PermissionRequest>?,
                                token: PermissionToken?
                            ) {
                                showRationaleDialogForPermissions()
                            }
                        }).onSameThread()
                        .check()
                }
            }
            // END

        }
    }

    private fun choosePhotoFromGallery() {
        // Here Dexter is used as an initial inquiry on whether the user wants to grant permissions or not
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
            // TODO Using the google places API (Step 6: Receive the valid result as we required from the Place Picker.)
            // START
            else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
                // Get place that is returned after clicking the etLocation
                val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                binding?.etLocation?.setText(place.address)
                mLatitude = place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude
            }
            // END
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

    // TODO Adding the select current location button and setting permissions (Step 3: Create a function to check the GPS is enabled or not.)
    // START
    /**
     * A function which is used to verify that the location or let's GPS is enable or not of the user's device.
     */
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }
    // END

    // TODO Getting The Users Location(Step 4: Create a location callback object of fused location provider client where we will get the current location details.)
    // START
    /**
     * A location callback object of fused location provider client where we will get the current location details.
     */
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            mLatitude = mLastLocation!!.latitude
            Log.e("Current Latitude", "$mLatitude")
            mLongitude = mLastLocation.longitude
            Log.e("Current Longitude", "$mLongitude")

            // TODO Get The Address From the Latitude and Longitude (Step 9: Call the AsyncTask class fot getting an address from the latitude and longitude.)
            // START
            // The background task has already done its magic(Geocoder)
            val addressTask =
                GetAddressFromLatLng(this@AddHappyPlace, mLatitude, mLongitude)
            // Use this instance to set the addressListener
            // Since you can't get the addressListener type externally use the internal one that already exists
            addressTask.setAddressListener(object :
                GetAddressFromLatLng.AddressListener {
                override fun onAddressFound(address: String?) {
                    Log.e("Address ::", "" + address)
                    binding?.etLocation?.setText(address) // Address is set to the edittext
                }

                override fun onError() {
                    Log.e("Get Address ::", "Something is wrong...")
                }
            })
            addressTask.getAddress() // Execute AsyncTask, all the code before is just setting up
            // END

        }
    }

    // TODO Getting The Users Location(Step 5: Create a method or let say function to request the current location. Using the fused location provider client.)
    // START
    /**
     * A function to request the current location. Using the fused location provider client.
     */
    // Permissions suppressed since the position where this method is used the necessary permissions will have been handled
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        // Interval can also be set to 0 since we are only asking for the location once
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        // Set to 1 since we are asking for the location only once
        mLocationRequest.numUpdates = 1
        // Already initialized in the onCreate method
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }




}