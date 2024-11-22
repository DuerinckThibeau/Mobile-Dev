package com.example.rentapp

import android.app.Application
import com.example.rentapp.workers.NotificationWorker

class RentifyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationWorker.schedule(this)
    }
} 