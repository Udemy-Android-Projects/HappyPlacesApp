package com.example.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.activities.AddHappyPlace
import com.example.happyplaces.activities.MainActivity
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.databinding.ItemHappyPlaceBinding
import com.example.happyplaces.models.HappyPlaceModel

// Open classes are the opposite of final.
open class HappyPlacesAdapter(
    private val context: Context,
    private var list: ArrayList<HappyPlaceModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Step 2 of making recycler view elements clickable is to create a variable for the onCLick listener interface
    private var onClickListener: OnClickListener? = null

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class MyViewHolder(binding: ItemHappyPlaceBinding) : RecyclerView.ViewHolder(binding.root) {
        val iv_place_image = binding.ivPlaceImage
        val tv_Title = binding.tvTitle
        val tv_Description = binding.tvDescription
    }
    /**
     * Inflates the item views which is designed in xml layout file
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            ItemHappyPlaceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            holder.iv_place_image.setImageURI(Uri.parse(model.image))
            holder.tv_Title.text = model.title
            holder.tv_Description.text = model.description
            // Step 5 assigning the onClickListener to every item that is created
            // itemView is a single row item in a list that contains a reference to a particular item at a particular position
            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }

    // Third step is to create a method that binds the onClickListener variable to the onClickListener that is passed
    // We do this since an adapter can not have/use an onClickListener
    // The actual onCLickListener will be in the class where the adapter is used
    // So the next step is implemented in the main activity
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }
    // Five step process to make recycler view items clickable
    // Step 1: Create an interface
    interface OnClickListener {
        fun onClick(position: Int, model: HappyPlaceModel)
    }

    // This method will be called in the main activity
    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int){
        val intent = Intent(context,AddHappyPlace::class.java)
        // Pass the object in question to be edited
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS,list[position])
        // Since intent can't be used in adapters the below format will start the activity
        // from the class where the adapter has been initialized

        // Expecting something back which in this case is the HappyPlacesModel we want to update
        activity.startActivityForResult(intent, requestCode)
        // Notify the adapter that data has been changed since we are editing an entry
        notifyItemChanged(position)
    }

    // TODO Swipe to delete (Step 3: Create a function to delete the happy place details which is inserted earlier from the local storage.)
    // START
    /**
     * A function to delete the added happy place detail from the local storage.
     */
    fun removeAt(position: Int) {

        val dbHandler = DatabaseHandler(context)
        // Delete from database
        val isDeleted = dbHandler.deleteHappyPlace(list[position])

        if (isDeleted > 0) {
            // Delete from arrayList
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
// END