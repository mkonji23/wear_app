/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.home_project

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.wear.widget.CurvedTextView
import androidx.wear.widget.WearableRecyclerView
import com.example.home_project.DataInterface.BusStationDataListener
import com.example.home_project.broadcast.MyReceiverMain
import com.example.home_project.broadcast.sender.BroadCastSender
import com.example.home_project.dataLayerAPI.DataChangeHandler
import com.example.home_project.dataLayerAPI.DataSenderToApp
import com.example.home_project.parcel.busParcel
import com.example.home_project.view.MyAdapter
import com.google.android.gms.wearable.Wearable
import com.google.gson.JsonObject
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import android.view.MotionEvent
import android.widget.Toast

class MainActivity : ComponentActivity(), BusStationDataListener {
    private lateinit var dataChangeHandler: DataChangeHandler
    private lateinit var dataSender: DataSenderToApp
    private var broadSender: BroadCastSender = BroadCastSender()
    private val myRceiver = MyReceiverMain()
    private var initTxt = "yet"
    private lateinit var recyclerView: WearableRecyclerView
    private var backgroundFlag = false;
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
            dataSender.setBusStationDataListener(this)
            // 브로드캐스트 등록
            val intentFilter = IntentFilter()
            intentFilter.addAction("MY_ACTION_TILE")
            intentFilter.addAction("MY_ACTION_WATCH")
            registerReceiver(myRceiver, intentFilter)
        } catch (e: Exception) {
            Log.e("MainActivity", "Layout inflate error", e)
        } finally {
            renderInit()
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        if (event != null && event.action == MotionEvent.ACTION_SCROLL) {
            // Rotary 이벤트 감지
            val rotaryDelta = event.getAxisValue(MotionEvent.AXIS_SCROLL)
            Log.e("onGenericMotionEvent", rotaryDelta.toString())
            if (rotaryDelta != 0f) {
                // 스크롤 처리
                recyclerView.scrollBy(0, -(rotaryDelta * 500).toInt())
                return true
            }

        }
        return super.onGenericMotionEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        // BroadcastReceiver 해제
        unregisterReceiver(myRceiver)
    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(dataChangeHandler)
        reqData()
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(dataChangeHandler)
    }


    @SuppressLint("SetTextI18n")
    override fun onBusStationDataReceived(jsonObject: JsonObject) {
        showLoading(false)
        Log.d("DataRevice", jsonObject.toString())
        val firstStation = jsonObject["firstStation"]?.asJsonObject;
        val secondStation = jsonObject["secondStation"]?.asJsonObject;
        val rtNm = firstStation?.get("rtNm")?.asString ?: "데이터 미전송"
        val stNm1 = firstStation?.get("stNm")?.asString ?: ""
        val arrmsg1 = firstStation?.get("arrmsg1")?.asString ?: ""
        val stNm2 = secondStation?.get("stNm")?.asString ?: ""
        val arrmsg1_2 = secondStation?.get("arrmsg1")?.asString ?: ""

        // 초기화 성공여부 체크
        initTxt = rtNm

        val homeParcel = busParcel(rtNm, stNm1, arrmsg1)
        val companyParcel = busParcel(rtNm, stNm2, arrmsg1_2)

        // 받은 데이터를 List에 표시
        var items = listOf(
            homeParcel,
            companyParcel
        )
        if (isTimeBetween9AMAnd2PM()) {
            items = items.reversed();
        }
        recyclerView.adapter = MyAdapter(items)

        if (arrmsg1 != null) {
            saveTileData(arrmsg1)
        }
        // 타일로 데이터 전송
        var busData = busParcel(rtNm ?: "", stNm1 ?: "", arrmsg1 ?: "");
        // 시간따라 세팅
        if (isTimeBetween9AMAnd2PM()) {
            busData = busParcel(rtNm ?: "", stNm2 ?: "", arrmsg1_2 ?: "");
        }
        broadSender.sendBroadcastRequest(this, "MY_ACTION_TILE", busData);
    }

    // 데이터 세팅 하고 딜레이
    override fun onBusStationDataSend() {
        Handler(Looper.getMainLooper()).postDelayed({
            showLoading(false)
            if (initTxt === "yet") {
                reqData();
                initTxt = "send1"
            }
            // 백그라운드서비스 여부 확인용
            if (!backgroundFlag) {
                val toast = Toast.makeText(this, "백그라운드서비스 OFF!", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.BOTTOM, 0, 0) // 화면 바텅에 표시
                toast.show()
            } else {
                backgroundFlag = false
            }
        }, 10000) //
    }

    override fun onBackServiceFlag(flag: Boolean) {
        backgroundFlag = flag
    }

    private fun saveTileData(data: String) {
        val prefs = getSharedPreferences("TileData", MODE_PRIVATE)
        prefs.edit().putString("busArrivalTime", data).apply()
    }

    // 시계앱 초기화
    private fun renderInit() {
        val refreshButton: ImageButton = findViewById(R.id.refreshButton)
        refreshButton.setOnClickListener {
            Log.d("refreshButtonClick", "refreshButton");
            reqData();
        }

        val items = listOf(
            busParcel("", "불러오는중...", "으앙"),
        )
        recyclerView = findViewById<WearableRecyclerView>(R.id.recycler_launcher_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        // 스크롤할 때 리스트의 첫 번째 및 마지막 아이템을 화면 중앙에 정렬 여부
        recyclerView.isEdgeItemsCenteringEnabled = true
//        recyclerView.isCircularScrollingGestureEnabled = true
        recyclerView.adapter = MyAdapter(items)

        // CurvedTextView 시간 설정
        val curvedTextClock = findViewById<CurvedTextView>(R.id.curvedTextClock)
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

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

    // 데이터 요청부분
    fun reqData() {
        showLoading(true)
        try {
            dataSender.requestData();
        } catch (err: Exception) {
            showLoading(false)
            Toast.makeText(this, "데이터 조회에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(flag: Boolean) {
        val loadingOverlay: FrameLayout = findViewById<FrameLayout>(R.id.loadingOverlay)
        if (flag) {
            loadingOverlay.visibility = View.VISIBLE
        } else {
            loadingOverlay.visibility = View.GONE
        }
    }

    // 시간 체크
    private fun isTimeBetween9AMAnd2PM(): Boolean {
        // 주어진 시간 문자열을 ZonedDateTime으로 변환하고 한국 시간(Asia/Seoul)으로 변환
        val zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("Asia/Seoul"))
        val parcelTime = zonedDateTime.toLocalTime()

        // 오전 9시부터 오후 2시 사이인지 확인
        val startTime = LocalTime.of(9, 0)  // 오전 9시
        val endTime = LocalTime.of(14, 0)   // 오후 2시

        return parcelTime.isAfter(startTime.minusSeconds(1)) && parcelTime.isBefore(
            endTime.plusSeconds(
                1
            )
        )
    }

}
