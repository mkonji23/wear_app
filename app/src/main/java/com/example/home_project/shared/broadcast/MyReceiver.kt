package com.example.home_project.shared.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.home_project.shared.parcel.busParcel
import com.example.home_project.shared.sharedPreference.SharedHandler
import com.example.home_project.tile.MainTileService

class MyReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onReceive(context: Context, intent: Intent) {
        val sharedHandler = SharedHandler(context)
        val data = intent.getParcelableExtra("data", busParcel::class.java)
        if (intent.action == "MY_ACTION_TILE") { // 타일 데이터 받아옴
            if (data != null) {
                sharedHandler.setTileData(data)
                if (context is MainTileService) {
                    Log.d("MyReceiver", "MY_ACTION_TILE updateTile")
                    context.updateTile()
                }
            }
        } else if (intent.action == "MY_ACTION_WATCH" && context is MainTileService) {
            Log.d("MyReceiver", "MY_ACTION_WATCH2 sendBroadcast")
            // MY_ACTION_WATCH로 데이터 전달
            val newIntent = Intent("MY_ACTION_WATCH2")
            context.sendBroadcast(newIntent)
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