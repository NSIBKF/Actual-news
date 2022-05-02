package com.example.myfirstapp

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDexApplication
import androidx.multidex.MultiDex
import io.realm.Realm
import io.realm.RealmConfiguration

class App:MultiDexApplication() {
    override fun attachBaseContext(base: Context?) { super.attachBaseContext(base)}

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)
        Realm.setDefaultConfiguration( RealmConfiguration.Builder().allowWritesOnUiThread(true).allowQueriesOnUiThread(true).build())
        getSharedPreferences("name", 0).edit().putString("zzz", "xx").apply()
        getSharedPreferences("name", 0).getString("zzz", "")
    }
}