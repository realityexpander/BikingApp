package com.realityexpander.bikingapp.di

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.realityexpander.bikingapp.db.RideDao
import com.realityexpander.bikingapp.db.RideDatabase
import com.realityexpander.bikingapp.common.Constants.Companion.DATABASE_NAME
import com.realityexpander.bikingapp.common.Constants.Companion.KEY_FIRST_TIME_TOGGLE
import com.realityexpander.bikingapp.common.Constants.Companion.KEY_HEIGHT
import com.realityexpander.bikingapp.common.Constants.Companion.KEY_NAME
import com.realityexpander.bikingapp.common.Constants.Companion.KEY_SORT_TYPE
import com.realityexpander.bikingapp.common.Constants.Companion.KEY_WEIGHT
import com.realityexpander.bikingapp.common.Constants.Companion.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * AppModule, provides application wide singletons
 */

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Weight

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Height

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
    fun provideRideDao(db: RideDatabase): RideDao {
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

//    @Weight
    @Singleton
    @Provides
    @Named("weight")
    fun provideWeight(sharedPreferences: SharedPreferences) =
        sharedPreferences.getFloat(KEY_WEIGHT, 80f)

//    @Height
    @Singleton
    @Provides
    @Named("height")
    fun provideHeight(sharedPreferences: SharedPreferences) =
        sharedPreferences.getFloat(KEY_HEIGHT, 100f)

    @Singleton
    @Provides
    @Named("String1")
    fun provideTestString1() = "This is a string 1 we will inject"

    @Singleton
    @Provides
    @Named("String2")
    fun provideTestString2() = "This is a string 2 we will inject"

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPreferences: SharedPreferences) = sharedPreferences.getBoolean(
        KEY_FIRST_TIME_TOGGLE, true
    )


}