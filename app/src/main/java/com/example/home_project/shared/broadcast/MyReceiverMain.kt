package com.example.home_project.shared.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.home_project.presentation.MainActivity
import com.example.home_project.shared.parcel.busParcel
import com.example.home_project.shared.sharedPreference.SharedHandler

class MyReceiverMain : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onReceive(context: Context, intent: Intent) {
        val sharedHandler = SharedHandler(context)
        val data = intent.getParcelableExtra("data", busParcel::class.java)
        if (intent.action == "MY_ACTION_WATCH2") {
            // 메인 스레드에서 MainActivity의 메서드를 호출
            if (context is MainActivity) {
                context.runOnUiThread {
                    // MainActivity에서 데이터 요청
                    context.reqData()
                }
            }
        }
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        StringBuilder().apply {
            append("location: MyReceiverMain\n")
            append("Action: ${intent.action}\n")
            append("URI: ${intent.toUri(Intent.URI_INTENT_SCHEME)}\n")
            append("DATA: ${data}\n")
            toString().also { log -> Log.d("BroadcastReceiver", log) }
        }
    }

}