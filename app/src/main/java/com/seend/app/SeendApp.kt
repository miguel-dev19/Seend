package com.seend.app

import android.app.Application
import com.seend.app.util.TokenManager

class SeendApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenManager.init(this)
    }
}
