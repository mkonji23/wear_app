package com.example.home_project.dataLayerAPI

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.example.home_project.DataInterface.BusStationDataListener
import com.example.home_project.contant.DataConstant
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import java.text.SimpleDateFormat
import kotlin.random.Random

// 안드로이드 앱으로 데이터 보내는 부분
class DataSenderToApp(context: Context) {
    private val dataClient: DataClient = Wearable.getDataClient(context)
    private var listener: BusStationDataListener? = null
    fun setBusStationDataListener(listener: BusStationDataListener) {
        this.listener = listener
    }

    @SuppressLint("SimpleDateFormat")
    fun requestData() {
        val sendTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis())
        val putDataReq: PutDataRequest = PutDataMapRequest.create("/getStationInfo").run {
            dataMap.putString(
                DataConstant.WEAR_KEY,
                sendTime
            )
            asPutDataRequest()
        }
        Log.d("DataSenderToApp", sendTime)
        val putDataTask: Task<DataItem> = dataClient.putDataItem(putDataReq)
        putDataTask.addOnSuccessListener {
            Log.d("DataSenderToApp", "Data sent successfully")
        }.addOnFailureListener { e ->
            Log.e("DataSenderToApp", "Failed to send data", e)
        }.addOnCompleteListener {
            Log.d("DataSenderToApp", "Data sent Complete")
            listener?.onBusStationDataSend();
            listener?.onBackServiceFlag(true);
        }
    }
}