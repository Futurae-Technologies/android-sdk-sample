package com.futurae.sampleapp

import android.app.Application
import com.futurae.sampleapp.utils.LocalStorage
import timber.log.Timber

class FuturaeSampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        LocalStorage.init(applicationContext)
    }
}