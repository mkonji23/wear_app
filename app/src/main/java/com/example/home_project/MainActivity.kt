/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.home_project

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.home_project.DataInterface.BusStationDataListener
import com.example.home_project.broadcast.MyReceiver
import com.example.home_project.broadcast.sender.BroadCastSender
import com.example.home_project.dataLayerAPI.DataChangeHandler
import com.example.home_project.dataLayerAPI.DataSenderToApp
import com.example.home_project.parcel.busParcel
import com.example.home_project.tile.MainTileService
import com.google.android.gms.wearable.Wearable
import com.google.gson.JsonObject

class MainActivity : ComponentActivity(), BusStationDataListener {
    private lateinit var dataChangeHandler: DataChangeHandler
    private lateinit var dataSender: DataSenderToApp
    private var broadSender: BroadCastSender =  BroadCastSender()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        try {
            Log.e("로그로그", "로그테스트")
            setContentView(R.layout.activity_main)
            // DataChangeHandler 초기화
            dataChangeHandler = DataChangeHandler(this)
            // 항상 초기화
            dataSender = DataSenderToApp(this)
            // 리스너 등록
            dataChangeHandler.setBusStationDataListener(this)

            dataSender.requestData();

        } catch (e: Exception) {
            Log.e("MainActivity", "Layout inflate error", e)
        }

        // 텍스트뷰와 버튼을 findViewById로 가져오기
        val textView: TextView = findViewById(R.id.busStopName)
        val refreshButton: ImageButton = findViewById(R.id.refreshButton)
        // 버튼 클릭 시 텍스트 변경
        refreshButton.setOnClickListener {
            textView.text = "sendTestData"
            dataSender.requestData();
        }
    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(dataChangeHandler)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(dataChangeHandler)
    }

    @SuppressLint("SetTextI18n")
    override fun onBusStationDataReceived(jsonObject: JsonObject) {
        Log.d("DataRevice", jsonObject.toString())
        val firstStation  = jsonObject["firstStation"]?.asJsonObject;
        val secondStation  = jsonObject["secondStation"]?.asJsonObject;

        val rtNm = firstStation?.get("rtNm")?.asString

        val stNm1 = firstStation?.get("stNm")?.asString
        val arrmsg1 = firstStation?.get("arrmsg1")?.asString

        val stNm2 = secondStation?.get("stNm")?.asString
        val arrmsg1_2 = secondStation?.get("arrmsg1")?.asString


        // 받은 데이터를 TextView에 표시
        val busStopName: TextView = findViewById(R.id.busStopName)
        val busArrivalTime: TextView = findViewById(R.id.busArrivalTime)
        val busName :TextView = findViewById(R.id.busName)
        busStopName.text = stNm1
        busArrivalTime.text = "다음 버스:${arrmsg1}"
        busName.text = rtNm
        if (arrmsg1 != null) {
            saveTileData(arrmsg1)
        }
        val busData = busParcel(rtNm ?: "",stNm1 ?: "",arrmsg1 ?: "");
        broadSender.sendBroadcastRequest(this,"MY_ACTION_TILE",busData);
    }

    private fun saveTileData(data: String) {
        val prefs = getSharedPreferences("TileData", Context.MODE_PRIVATE)
        prefs.edit().putString("busArrivalTime", data).apply()
    }

}
