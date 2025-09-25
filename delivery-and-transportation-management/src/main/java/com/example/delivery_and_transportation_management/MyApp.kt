package com.example.delivery_and_transportation_management

import android.app.Application
import android.preference.PreferenceManager
import org.osmdroid.config.Configuration


class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        // 设置 userAgent，避免 OSM 封锁请求
        Configuration.getInstance().userAgentValue = applicationContext.packageName
    }
}
