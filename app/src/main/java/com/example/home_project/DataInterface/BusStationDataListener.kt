package com.example.home_project.DataInterface

import com.google.gson.JsonObject

interface BusStationDataListener {
    fun onBusStationDataReceived(jsonObject: JsonObject)
    fun onBusStationDataSend()
    fun onBackServiceFlag(flag: Boolean)
}