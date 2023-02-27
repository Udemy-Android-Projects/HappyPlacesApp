package com.example.happyplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.happyplaces.adapters.HappyPlacesAdapter
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.databinding.ActivityMainBinding
import com.example.happyplaces.models.HappyPlaceModel


class MainActivity : AppCompatActivity() {

    companion object {
        private val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
    }

    private var binding : ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.fabAddHappyPlace?.setOnClickListener {
            val intent = Intent(this, AddHappyPlace::class.java)
            /* We want to get live updates when we save a happy place.
               We use startActivityForResult to provide us with a way to confirm if data has been successfully added to the database
               At the point where data is added the code {setResult(Activity.RESULT_OK)} will confirm this
             */
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }
        getHappyPlaceListFromLocalDB()
    }

    private fun getHappyPlaceListFromLocalDB() {
        // We have to create an object of the database handler
        val dbHandler = DatabaseHandler(this)
        val getHappyPLaceList : ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()
        if(getHappyPLaceList.size > 0) {
            binding?.rvHappyPlacesList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE
            setUpHappyPLacesRecyclerView(getHappyPLaceList)
        } else {
            binding?.rvHappyPlacesList?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }

    /**
     * A function to populate the recyclerview to the UI.
     */
    private fun setUpHappyPLacesRecyclerView(happyPLaceList: ArrayList<HappyPlaceModel>) {
        binding?.rvHappyPlacesList?.layoutManager = LinearLayoutManager(this)
        // binding?.rvHappyPlacesList?.setHasFixedSize(true)
        val placesAdapter = HappyPlacesAdapter(happyPLaceList)
        binding?.rvHappyPlacesList?.adapter = placesAdapter
        // Step 4 of making a recycler view item clickable
        // Implementing the onClickListener here
        placesAdapter.setOnClickListener(
            object: HappyPlacesAdapter.OnClickListener{
                override fun onClick(position: Int, model: HappyPlaceModel) {
                    // Here we implement the logic for the onClickListener
                    val intent = Intent(this@MainActivity, HappyPlaceDetailActivity::class.java)
                    startActivity(intent)
                }

            }
        )
    }

    // If we have successfully added data to the database we can implement the onActivityResult method
    // to dynamically update the recyclerView
    // As usual this is run automatically once startActivityForResult is called
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK) {
                // Get the updated happy places list
                getHappyPlaceListFromLocalDB()
            } else {
                Log.e("Activity", "Cancelled or Back pressed")
            }
        }
    }
}






