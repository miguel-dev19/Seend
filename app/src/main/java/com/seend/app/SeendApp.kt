package com.seend.app

import android.app.Application
import android.util.Log
import com.seend.app.di.AppModule
import com.seend.app.util.TokenManager

class SeendApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenManager.init(this)
        AppModule.initDatabase(this)
        Log.d("Seend", "App iniciada - BD lista")
    }
}
