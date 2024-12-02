package com.example.home_project.dataLayerAPI

import android.content.Context
import android.util.Log
import com.example.home_project.DataInterface.BusStationDataListener
import com.example.home_project.contant.DataConstant
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import com.google.gson.JsonObject
import com.google.gson.JsonParser

// 데이터 변경 감지
class DataChangeHandler(private val context: Context) : DataClient.OnDataChangedListener {
    private var listener: BusStationDataListener? = null

    fun setBusStationDataListener(listener: BusStationDataListener) {
        this.listener = listener
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                event.dataItem.also { item ->
                    when (item.uri.path) {
                        "/count" -> handleCountData(item)
                        "/setStationInfo" -> handleBusStationData(item)
                        else -> Log.w("DataChangeHandler", "Unhandled path: ${item.uri.path}")
                    }
                }
            } else if (event.type == DataEvent.TYPE_DELETED) {
                Log.d("DataChangeHandler", "Data item deleted: ${event.dataItem.uri}")
            }
        }
    }

    private fun handleCountData(item: DataItem) {
        val count = DataMapItem.fromDataItem(item).dataMap.getInt(DataConstant.WEAR_KEY)
        Log.d("DataChangeHandler", "Count updated: $count")
    }

    private fun handleBusStationData(item: DataItem) {
        val dataMap = DataMapItem.fromDataItem(item).dataMap
        val jsonString = dataMap.getString("arguments", "")
        val jsonObject: JsonObject = JsonParser.parseString(jsonString).asJsonObject
        Log.d("DataChangeHandler", "Bus station updated: $jsonString")
        // 데이터 전달
        listener?.onBusStationDataReceived(jsonObject)
    }
}
