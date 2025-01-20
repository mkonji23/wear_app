package com.example.home_project.shared.broadcast.sender

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.home_project.shared.parcel.busParcel


class BroadCastSender {
    fun sendBroadcastRequest(context: Context, action: String, params: busParcel? = null) {
        val intent = Intent()
        intent.action = (action)
        intent.setClassName(context, "com.example.home_project.shared.broadcast.MyReceiver")
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        intent.setPackage("com.example.home_project")
        intent.putExtra("data", params)
        context.sendBroadcast(intent)
        Log.d("MainTileService", "Broadcast sent with action $action")
    }
}