/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.home_project

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.wear.widget.CurvedTextView
import androidx.wear.widget.WearableRecyclerView
import com.example.home_project.DataInterface.BusStationDataListener
import com.example.home_project.broadcast.sender.BroadCastSender
import com.example.home_project.dataLayerAPI.DataChangeHandler
import com.example.home_project.dataLayerAPI.DataSenderToApp
import com.example.home_project.parcel.busParcel
import com.example.home_project.view.MyAdapter
import com.google.android.gms.wearable.Wearable
import com.google.gson.JsonObject
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class MainActivity : ComponentActivity(), BusStationDataListener {
    private lateinit var dataChangeHandler: DataChangeHandler
    private lateinit var dataSender: DataSenderToApp
    private var broadSender: BroadCastSender =  BroadCastSender()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        try {
            Log.e("onCreate", "onCreate")
            setContentView(R.layout.activity_main)
            // DataChangeHandler 초기화
            dataChangeHandler = DataChangeHandler(this)
            // 항상 초기화
            dataSender = DataSenderToApp(this)
            // 리스너 등록
            dataChangeHandler.setBusStationDataListener(this)


        } catch (e: Exception) {
            Log.e("MainActivity", "Layout inflate error", e)
        }

//        val mainLayout = findViewById<LinearLayout>(R.id.mainLayout)
//        // 텍스트뷰와 버튼을 findViewById로 가져오기
//        val textView: TextView = findViewById(R.id.busStopName)
        val refreshButton: ImageButton = findViewById(R.id.refreshButton)
        val progressBar : ProgressBar = findViewById(R.id.progressBar)
//        // 버튼 클릭 시 텍스트 변경
        refreshButton.setOnClickListener {
//            mainLayout.visibility = View.GONE
//            progressBar.visibility =View.VISIBLE
//            textView.text = "데이터 조회 중..."
            dataSender.requestData();
        }

        val items = listOf(
            busParcel("",  "불러오는중...", "으앙"),
        )
        val recyclerView = findViewById<WearableRecyclerView>(R.id.recycler_launcher_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MyAdapter(items)

        // CurvedTextView 시간 설정
        val curvedTextClock = findViewById<CurvedTextView>(R.id.curvedTextClock)
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        // 시간 업데이트
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    curvedTextClock.text = dateFormat.format(Date())
                }
            }
        }, 0, 1000) // 1초 간격 업데이트
    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(dataChangeHandler)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(dataChangeHandler)
    }

    override fun onStart() {
        super.onStart()
        // 데이터 요청
        Handler(Looper.getMainLooper()).postDelayed({
            dataSender.requestData();
        }, 1000) //
    }

    @SuppressLint("SetTextI18n")
    override fun onBusStationDataReceived(jsonObject: JsonObject) {
        Log.d("DataRevice", jsonObject.toString())
        val firstStation  = jsonObject["firstStation"]?.asJsonObject;
        val secondStation  = jsonObject["secondStation"]?.asJsonObject;
        val rtNm = firstStation?.get("rtNm")?.asString ?: "데이터 미전송"
        val stNm1 = firstStation?.get("stNm")?.asString ?: ""
        val arrmsg1 = firstStation?.get("arrmsg1")?.asString ?: ""
        val stNm2 = secondStation?.get("stNm")?.asString ?: ""
        val arrmsg1_2 = secondStation?.get("arrmsg1")?.asString ?: ""

        // 받은 데이터를 List에 표시
        val items = listOf(
            busParcel(rtNm,  stNm1, arrmsg1),
            busParcel(rtNm , stNm2, arrmsg1_2),
        )
        val recyclerView = findViewById<WearableRecyclerView>(R.id.recycler_launcher_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MyAdapter(items)

        if (arrmsg1 != null) {
            saveTileData(arrmsg1)
        }
        // 타일로 데이터 전송
        val busData = busParcel(rtNm ?: "",stNm1 ?: "",arrmsg1 ?: "");
        broadSender.sendBroadcastRequest(this,"MY_ACTION_TILE",busData);
    }

    private fun saveTileData(data: String) {
        val prefs = getSharedPreferences("TileData", Context.MODE_PRIVATE)
        prefs.edit().putString("busArrivalTime", data).apply()
    }

}
