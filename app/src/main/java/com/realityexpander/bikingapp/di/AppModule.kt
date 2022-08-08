package com.realityexpander.bikingapp.di

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.realityexpander.bikingapp.db.RideDao
import com.realityexpander.bikingapp.db.RideDatabase
import com.realityexpander.bikingapp.other.Constants.Companion.DATABASE_NAME
import com.realityexpander.bikingapp.other.Constants.Companion.KEY_FIRST_TIME_TOGGLE
import com.realityexpander.bikingapp.other.Constants.Companion.KEY_NAME
import com.realityexpander.bikingapp.other.Constants.Companion.KEY_WEIGHT
import com.realityexpander.bikingapp.other.Constants.Companion.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * AppModule, provides application wide singletons
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAppDb(app: Application): RideDatabase {
        return Room.databaseBuilder(app, RideDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideRunDao(db: RideDatabase): RideDao {
        return db.getRideDao()
    }

    @Singleton
    @Provides
    fun provideSharedPreferences(app: Application) =
        app.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPreferences: SharedPreferences) =
        sharedPreferences.getString(KEY_NAME, "") ?: ""

    @Singleton
    @Provides
    fun provideWeight(sharedPreferences: SharedPreferences) =
        sharedPreferences.getFloat(KEY_WEIGHT, 80f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPreferences: SharedPreferences) = sharedPreferences.getBoolean(
        KEY_FIRST_TIME_TOGGLE, true
    )


}