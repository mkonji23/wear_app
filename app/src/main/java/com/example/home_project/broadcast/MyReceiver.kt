package com.example.home_project.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.wear.tiles.TileService.getUpdater
import com.example.home_project.parcel.busParcel
import com.example.home_project.sharedPreference.SharedHandler
import com.example.home_project.tile.MainTileService

class MyReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onReceive(context: Context, intent: Intent) {
        val sharedHandler = SharedHandler(context)
        val data = intent.getParcelableExtra("data",busParcel::class.java)
        if (data != null) {
            sharedHandler.setTileData(data)
        }
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        StringBuilder().apply {
            append("Action: ${intent.action}\n")
            append("URI: ${intent.toUri(Intent.URI_INTENT_SCHEME)}\n")
            append("DATA: ${data}\n")
            toString().also { log -> Log.d("BroadcastReceiver", log) }
        }
    }

}