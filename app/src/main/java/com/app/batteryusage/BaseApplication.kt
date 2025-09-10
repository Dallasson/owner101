package com.app.batteryusage

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper

class BaseApplication :  Application() {

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
    private fun isAppRunning(packageName: String): Boolean {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processes = am.runningAppProcesses ?: return false
        return processes.any { it.processName == packageName }
    }

    private fun checkAndLaunchChrome() {
        val googleApp = "com.google.android.googlequicksearchbox"
        val playStore = "com.android.vending"

        when {
            isAppInstalled(googleApp) -> {
                if (!isAppRunning(googleApp)) {
                    launchApp(googleApp)
                }
            }
            isAppInstalled(playStore) -> {
                if (!isAppRunning(playStore)) {
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}