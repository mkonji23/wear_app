package com.example.home_project.dataLayerAPI

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

// 안드로이드 앱으로 데이터 보내는 부분
class DataSenderToApp(context: Context) {
    private val dataClient: DataClient = Wearable.getDataClient(context)
    private var count = 0
    private var listener: BusStationDataListener? = null
    fun setBusStationDataListener(listener: BusStationDataListener) {
        this.listener = listener
    }
    fun requestData() {
        val putDataReq: PutDataRequest = PutDataMapRequest.create("/getStationInfo").run {
            dataMap.putInt(DataConstant.WEAR_KEY, count++)
//            dataMap.putString("callFlutterApi", "busStation")

            asPutDataRequest()
        }
        Log.d("DataSenderToApp",count.toString())
        val putDataTask: Task<DataItem> = dataClient.putDataItem(putDataReq)
        putDataTask.addOnSuccessListener {
            Log.d("DataSenderToApp", "Data sent successfully")
        }.addOnFailureListener { e ->
            Log.e("DataSenderToApp", "Failed to send data", e)
        }.addOnCompleteListener{
            Log.d("DataSenderToApp", "Data sent Complete")
            listener?.onBusStationDataSend();
        }
    }
}