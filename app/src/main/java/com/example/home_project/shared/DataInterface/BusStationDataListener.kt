package com.example.home_project.shared.DataInterface

import com.google.gson.JsonObject

interface BusStationDataListener {
    fun onBusStationDataReceived(jsonObject: JsonObject)
    fun onBusStationDataSend()
    fun onBackServiceFlag(flag: Boolean)
}