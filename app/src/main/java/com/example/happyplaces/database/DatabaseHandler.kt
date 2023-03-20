package com.example.happyplaces.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.happyplaces.models.HappyPlaceModel

class DatabaseHandler(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION)
{
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "HappyPlacesDatabase"
        private const val TABLE_HAPPY_PLACE = "HappyPlacesTable"
        // All the column names
        private const val KEY_ID = "_id"
        private const val KEY_TITLE = "title"
        private const val KEY_IMAGE = "image"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_DATE = "date"
        private const val KEY_LOCATION = "location"
        private const val KEY_LONGITUDE = "longitude"
        private const val KEY_LATITUDE = "latitude"
    }

    // Creates a new database if it doesn't exist
    override fun onCreate(db: SQLiteDatabase?) {
        // Creating a table with fields
        // We stored everything in form of text including the latitude nad longitude since it creates a less comlicated database
        val CREATE_HAPPY_PLACE_TABLE = ("CREATE TABLE " + TABLE_HAPPY_PLACE + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " TEXT,"
                + KEY_IMAGE + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_DATE + " TEXT,"
                + KEY_LOCATION + " TEXT,"
                + KEY_LATITUDE + " TEXT,"
                + KEY_LONGITUDE + " TEXT)")
        // Execute this query
        db?.execSQL(CREATE_HAPPY_PLACE_TABLE)
    }

    // Will have a singleton structure
    // This means that we will only have one instance of a database at any given time
    // This method is called if we want to make changes to the database i.e any crud operation apart from read of course
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_HAPPY_PLACE")
        onCreate(db)
    }

    // Returns a long that indicates if the operation succeeded or not
    // We pass a happyPlace object so that we can get the data we want to store
    fun addHappyPlace(happyPlace: HappyPlaceModel): Long {
        // Needed since we want to write to the database
        val db = this.writableDatabase

        val contentValues = ContentValues()
        // Put data into content values. We get the data from the object we passed
        contentValues.put(KEY_TITLE, happyPlace.title) // HappyPlaceModelClass TITLE
        contentValues.put(KEY_IMAGE, happyPlace.image) // HappyPlaceModelClass IMAGE
        contentValues.put(
            KEY_DESCRIPTION,
            happyPlace.description
        ) // HappyPlaceModelClass DESCRIPTION
        contentValues.put(KEY_DATE, happyPlace.date) // HappyPlaceModelClass DATE
        contentValues.put(KEY_LOCATION, happyPlace.location) // HappyPlaceModelClass LOCATION
        contentValues.put(KEY_LATITUDE, happyPlace.latitude) // HappyPlaceModelClass LATITUDE
        contentValues.put(KEY_LONGITUDE, happyPlace.longitude) // HappyPlaceModelClass LONGITUDE

        // Inserting Row
        // db.close() // Closing database connection will prevent the database inspector from showing hte database contents
        return db.insert(TABLE_HAPPY_PLACE, null, contentValues)
    }

    // TODO Fixing Update Bug (Step 1: Creating a function to edit/update the existing happy place detail.)
    // Int is returned since the update method returns an int while the insert method returns a Long
    fun updateHappyPlace(happyPlace: HappyPlaceModel): Int {
        // Needed since we want to write to the database
        val db = this.writableDatabase

        val contentValues = ContentValues()
        // Put data into content values. We get the data from the object we passed
        contentValues.put(KEY_TITLE, happyPlace.title) // HappyPlaceModelClass TITLE
        contentValues.put(KEY_IMAGE, happyPlace.image) // HappyPlaceModelClass IMAGE
        contentValues.put(
            KEY_DESCRIPTION,
            happyPlace.description
        ) // HappyPlaceModelClass DESCRIPTION
        contentValues.put(KEY_DATE, happyPlace.date) // HappyPlaceModelClass DATE
        contentValues.put(KEY_LOCATION, happyPlace.location) // HappyPlaceModelClass LOCATION
        contentValues.put(KEY_LATITUDE, happyPlace.latitude) // HappyPlaceModelClass LATITUDE
        contentValues.put(KEY_LONGITUDE, happyPlace.longitude) // HappyPlaceModelClass LONGITUDE

        // db.close() // Closing database connection will prevent the database inspector from showing hte database contents
        // Update an entry
        return db.update(TABLE_HAPPY_PLACE, contentValues, KEY_ID + "=" + happyPlace.id, null)
    }

    fun getHappyPlacesList() : ArrayList<HappyPlaceModel> {
        val happyPlaceList = java.util.ArrayList<HappyPlaceModel>()
        val selectQuery = "SELECT * from $TABLE_HAPPY_PLACE"
        // For adding data we used writable database
        val db = this.readableDatabase
        try {
            // Used to iterate through every entry we have selected from our table
            val cursor : Cursor? = db.rawQuery(selectQuery,null)
            // Move to first entry of cursor
            if(cursor!!.moveToFirst()) {
                do{
                    // Create happyPlaces that will be stored in the arrayList
                    val happyPlace = HappyPlaceModel(
                        // Get info using the cursor
                        // The OrThrow was added in order to throw an error in case the column doesn't exist
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOCATION)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LONGITUDE))
                    )
                    happyPlaceList.add(happyPlace)
                }while(cursor.moveToNext()) // Used like an iterator
            }
            cursor.close()
        }catch(e : SQLiteException) {
            db.execSQL(selectQuery)
            // return an empty arraylist
            return ArrayList()
        }
        return happyPlaceList
    }
    // TODO (Step 4: Creating a function to delete the existing happy place detail.)
    // START
    /**
     * Function to delete happy place details.
     */
    fun deleteHappyPlace(happyPlace: HappyPlaceModel): Int {
        val db = this.writableDatabase
        // Deleting Row
        val success = db.delete(TABLE_HAPPY_PLACE, KEY_ID + "=" + happyPlace.id, null)
        //2nd argument is String containing nullColumnHack
        return success
    }
    // END
}
