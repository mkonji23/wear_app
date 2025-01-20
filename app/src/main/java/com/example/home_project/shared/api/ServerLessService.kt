package com.example.home_project.shared.api

import com.example.home_project.shared.contant.DataConstant

class ServerLessService {
    fun getStationTimeByIds(arsId1: String, arsId2: String, callback: ApiCallback) {
        val apiService = ApiService()
        val paramMap = mapOf(
            // JSON 데이터를 맵 형식으로 준비
            "arsId1" to arsId1,
            "arsId2" to arsId2,
        )
        val url = "${DataConstant.apiEndPoint}/bus/getStationTimeByIds"

        apiService.makeApiCall(url, paramMap, "GET", callback)
    }
}