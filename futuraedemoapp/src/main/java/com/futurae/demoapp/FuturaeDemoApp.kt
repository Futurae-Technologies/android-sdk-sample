package com.futurae.demoapp

import android.app.Application
import com.futurae.demoapp.utils.LocalStorage
import timber.log.Timber

class FuturaeDemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        LocalStorage.init(applicationContext)
    }
}