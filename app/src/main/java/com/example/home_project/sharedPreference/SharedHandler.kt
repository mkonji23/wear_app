package com.example.home_project.sharedPreference

import android.content.Context
import android.content.SharedPreferences
import com.example.home_project.parcel.busParcel
import com.google.gson.Gson

class SharedHandler(private val context: Context) {
    fun setTileData(data: busParcel) {
        val prefs: SharedPreferences =
            context.getSharedPreferences("TileData", Context.MODE_PRIVATE)
        val gson = Gson()
        val jsonData = gson.toJson(data)
        prefs.edit().putString("data", jsonData).apply()
    }

    fun getTileData(): String {
        val prefs: SharedPreferences =
            context.getSharedPreferences("TileData", Context.MODE_PRIVATE)
        return prefs.getString("data", null) ?: "noData"
    }
}