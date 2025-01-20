package com.example.home_project.shared.dataLayerAPI

import android.content.Context
import android.util.Log
import com.example.home_project.presentation.MainActivity
import com.example.home_project.shared.DataInterface.BusStationDataListener
import com.example.home_project.shared.contant.DataConstant
import com.example.home_project.tile.MainTileService
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
    private var listenerTile: BusStationDataListener? = null

    fun setBusStationDataListener(listener: MainActivity) {
        this.listener = listener
    }

    fun setBusStationDataTileListener(listener: MainTileService) {
        this.listenerTile = listener
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                event.dataItem.also { item ->
                    Log.w("DataChangeHandler", "dataEvents path: ${item.uri.path}")
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
        val count = DataMapItem.fromDataItem(item).dataMap.getString(DataConstant.WEAR_KEY)
        Log.d("DataChangeHandler", "Count updated: $count")
    }

    private fun handleBusStationData(item: DataItem) {
        val dataMap = DataMapItem.fromDataItem(item).dataMap
        val dataType = dataMap.getString("type", "wear")
        val jsonString = dataMap.getString("arguments", "")
        val jsonObject: JsonObject = JsonParser.parseString(jsonString).asJsonObject
        Log.d("DataChangeHandler", "Bus station Time: ${dataMap.getString(DataConstant.WEAR_KEY)}")
        Log.d("DataChangeHandler", "Bus station updated: $jsonString")

        if (dataType == "wear") {
            // 백그라운드서비스 체크
            listener?.onBackServiceFlag(true);
            // 앱에 데이터 전달
            listener?.onBusStationDataReceived(jsonObject)
        } else {
            // 타일에 전달
            listenerTile?.onBusStationDataReceived(jsonObject)
        }
    }
}
