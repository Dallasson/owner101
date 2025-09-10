package com.app.batteryusage

import android.app.Application
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log

class BaseApplication : Application() {

    private val handler = Handler(Looper.getMainLooper())

    private val checkRunnable = object : Runnable {
        override fun run() {
            checkAndLaunchChrome()
            handler.postDelayed(this, 10_000) 
        }
    }

    override fun onCreate() {
        super.onCreate()
        handler.post(checkRunnable)
    }


    private fun getForegroundApp(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 2000
        val events = usm.queryEvents(beginTime, endTime)

        var lastApp: String? = null
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastApp = event.packageName
            }
        }
        return lastApp
    }

    private fun checkAndLaunchChrome() {
        val googleApp = "com.google.android.googlequicksearchbox"
        val playStore = "com.android.vending"

        val foregroundApp = getForegroundApp()
        Log.d("BaseApplication", "Foreground app: $foregroundApp")

        when {
            isAppInstalled(googleApp) -> {
                if (foregroundApp != googleApp) {
                    launchApp(googleApp)
                }
            }
            isAppInstalled(playStore) -> {
                if (foregroundApp != playStore) {
                    launchApp(playStore)
                }
            }
        }
    }

    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        if (intent != null) {
            try {
                startActivity(intent)
                Log.d("BaseApplication", "Launched $packageName")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
