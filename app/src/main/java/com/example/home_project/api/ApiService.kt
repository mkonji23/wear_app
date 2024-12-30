package com.example.home_project.api

import com.example.home_project.contant.DataConstant
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.net.URLEncoder

// api 서비스 호출방식 (인터넷이 연결되어있을때 사용)
interface ApiCallback {
    fun onSuccess(responseData: String)
    fun onError(errorMessage: String)
}

class ApiService {
    private val client = OkHttpClient()
    private val gson = Gson() // JSON 변환을 위한 Gson 인스턴스

    @Throws(Exception::class)
    fun makeApiCall(
        apiUrl: String,
        payload: Map<String, Any>,
        method: String = "POST",
        callback: ApiCallback
    ) {
        // JSON 데이터로 변환
        val jsonPayload = gson.toJson(payload) // Map을 JSON 문자열로 변환
        // 요청 본문 작성
        val body = jsonPayload
            .toRequestBody("application/json; charset=utf-8".toMediaType())
        // 요청 객체 생성
        var request = Request.Builder()
            .header("x-api-key", DataConstant.nodeApiKey)
            .url(apiUrl)
            .post(body) // GET 요청의 경우 .get() 사용
            .build()

        if ("GET" == method.uppercase()) {
            val queryParam = payload.entries.joinToString("&") { (key, value) ->
                "${URLEncoder.encode(key, "UTF-8")}=${
                    URLEncoder.encode(
                        value.toString(),
                        "UTF-8"
                    )
                }"
            }

            val queryUrl = "${apiUrl}?${queryParam}"

            request = Request.Builder()
                .header("x-api-key", DataConstant.nodeApiKey)
                .url(queryUrl)
                .get() // GET 요청의 경우 .get() 사용
                .build()
        }

        // 비동기 요청 처리
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 네트워크 오류 처리
                callback.onError("Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // 응답 성공 시 처리
                    val responseData = response.body?.string() ?: "Empty response"
                    callback.onSuccess(responseData)
                } else {
                    // 응답 실패 시 처리
                    callback.onError("HTTP Error: ${response.code}")
                }
            }
        })
    }
}