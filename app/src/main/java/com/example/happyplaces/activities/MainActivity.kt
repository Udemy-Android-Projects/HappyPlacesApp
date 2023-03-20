package com.example.happyplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.adapters.HappyPlacesAdapter
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.databinding.ActivityMainBinding
import com.example.happyplaces.models.HappyPlaceModel
import com.example.happyplaces.utils.SwipeToDeleteCallback
import com.example.happyplaces.utils.SwipeToEditCallback


class MainActivity : AppCompatActivity() {

    companion object {
        private const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        const val EXTRA_PLACE_DETAILS = "extra_place_details"
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
    private fun setUpHappyPLacesRecyclerView(happyPlaceList: ArrayList<HappyPlaceModel>) {
        binding?.rvHappyPlacesList?.layoutManager = LinearLayoutManager(this)
        // binding?.rvHappyPlacesList?.setHasFixedSize(true)
        val placesAdapter = HappyPlacesAdapter(this,happyPlaceList)
        binding?.rvHappyPlacesList?.adapter = placesAdapter
        // Step 4 of making a recycler view item clickable
        // Implementing the onClickListener here
        placesAdapter.setOnClickListener(
            object: HappyPlacesAdapter.OnClickListener{
                override fun onClick(position: Int, model: HappyPlaceModel) {
                    // Here we implement the logic for the onClickListener
                    val intent = Intent(this@MainActivity, HappyPlaceDetailActivity::class.java)
                    // We are passing the entire model to the other class
                    // In order for this to work out class has to either serializable or parcelable
                    intent.putExtra(EXTRA_PLACE_DETAILS, model)
                    startActivity(intent)
                }

            }
        )

        // Binding the swipe feature to recycler view items
        val editSwipeHandler = object: SwipeToEditCallback(this) {
            // Override the functionality that describes what happens when swiping occurs
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Notify the adapter on changes about to be made
                // Create new adapter
                val adapter = binding?.rvHappyPlacesList?.adapter as HappyPlacesAdapter
                // Notify the recyclerview that some change as occur
                // This method then opens up the addHappyPlaceActivity
                adapter.notifyEditItem(this@MainActivity,viewHolder.adapterPosition,
                    ADD_PLACE_ACTIVITY_REQUEST_CODE)
            }
        }
        // TODO (Step 9: Attaching the swipeHandler to the recycler view item.)
        // START
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(binding?.rvHappyPlacesList)


        // TODO Swipe to delete(Step 2: Bind the delete feature class to recyclerview)
        val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // TODO Swipe to delete(Step 5: Call the adapter function when it is swiped for delete)
                // START
                val adapter = binding?.rvHappyPlacesList?.adapter as HappyPlacesAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                getHappyPlaceListFromLocalDB() // Gets the latest list from the local database after item being delete from it.
                // END
            }
        }
        // TODO Swipe to delete(Step 6: Attaching the swipeHandler to the recycler view item.)
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(binding?.rvHappyPlacesList)
        // END
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






