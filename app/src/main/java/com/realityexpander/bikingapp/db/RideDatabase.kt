package com.realityexpander.bikingapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Ride::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class RideDatabase : RoomDatabase() {

    abstract fun getRideDao(): RideDao
}