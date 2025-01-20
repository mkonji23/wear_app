package com.example.home_project.tile

import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.EventBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.example.home_project.shared.DataInterface.BusStationDataListener
import com.example.home_project.shared.broadcast.MyReceiver
import com.example.home_project.shared.dataLayerAPI.DataChangeHandler
import com.example.home_project.shared.dataLayerAPI.DataSenderToApp
import com.example.home_project.shared.parcel.busParcel
import com.example.home_project.shared.sharedPreference.SharedHandler
import com.google.android.gms.wearable.Wearable
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.compose.tools.buildDeviceParameters
import com.google.android.horologist.tiles.SuspendingTileService
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

private var RESOURCES_VERSION = 0

/**
 * Skeleton for a tile with no images.
 */
@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService(), BusStationDataListener {
    private val myRceiver = MyReceiver()

    //    private val mySender = BroadCastSender()
    private val sharedHandler = SharedHandler(this)
    private lateinit var dataSender: DataSenderToApp
    private lateinit var dataChangeHandler: DataChangeHandler
    override fun onCreate() {
        super.onCreate()
        // DataChangeHandler 초기화
        dataChangeHandler = DataChangeHandler(this)
        dataChangeHandler.setBusStationDataTileListener(this)
        // 항상 초기화
        dataSender = DataSenderToApp(this)
        Wearable.getDataClient(this).addListener(dataChangeHandler)
    }

    override fun onDestroy() {
        super.onDestroy()
        Wearable.getDataClient(this).removeListener(dataChangeHandler)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onTileAddEvent(requestParams: EventBuilders.TileAddEvent) {
        super.onTileAddEvent(requestParams)
        // 동적으로 BroadcastReceiver 등록
        val intentFilter = IntentFilter()
        intentFilter.addAction("MY_ACTION_TILE")
        intentFilter.addAction("MY_ACTION_WATCH")
        registerReceiver(myRceiver, intentFilter, RECEIVER_NOT_EXPORTED)
        Log.d("MainTileService", "Receiver registered")

    }

    override fun onTileRemoveEvent(requestParams: EventBuilders.TileRemoveEvent) {
        super.onTileRemoveEvent(requestParams)
        // 리시버 등록 해제
        try {
            unregisterReceiver(myRceiver)
        } catch (e: IllegalArgumentException) {
            // 리시버가 등록되지 않았을 때의 예외 처리
            Log.e("MainTileService", "Receiver already unregistered or not registered")
        }
    }

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder().setVersion(RESOURCES_VERSION.toString())
            .build()
    }

    override fun onTileLeaveEvent(requestParams: EventBuilders.TileLeaveEvent) {
        super.onTileLeaveEvent(requestParams)
    }

    override fun onTileEnterEvent(requestParams: EventBuilders.TileEnterEvent) {
        super.onTileEnterEvent(requestParams)
        // 타일 갱신이 필요한 경우 -  타일 -> 모바일 앱으로 요청
        dataSender.requestData("tile");
    }


    // 타일 업데이트 이벤트
    fun updateTile() {
        RESOURCES_VERSION++
        getUpdater(this).requestUpdate(MainTileService::class.java)
    }

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val lastClickableId = requestParams.currentState.lastClickableId
        val sharedData = sharedHandler.getTileData();
        Log.d("tileRequest", "sharedData: $sharedData")
        Log.d("tileRequest", "lastClickableId: $lastClickableId")

        val multiTileTimeline = TimelineBuilders.Timeline.fromLayoutElement(
            when (lastClickableId) {
                "foo" -> tileLayout(this, sharedData)
                else -> tileLayout(this, sharedData)
            }
        )
        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION.toString())  // 고유 버전 생성
            .setTileTimeline(multiTileTimeline)
            .setFreshnessIntervalMillis(900_000) // 15분 마다 갱신
            .build()
    }

    override fun onBusStationDataReceived(jsonObject: JsonObject) {
        Log.d("onBusStationDataReceived", "onBusStationDataReceived: $jsonObject")
        val firstStation = jsonObject["firstStation"]?.asJsonObject;
        val secondStation = jsonObject["secondStation"]?.asJsonObject;
        val rtNm = firstStation?.get("rtNm")?.asString ?: "데이터 미전송"
        val stNm1 = firstStation?.get("stNm")?.asString ?: ""
        val arrmsg1 = firstStation?.get("arrmsg1")?.asString ?: ""
        val stNm2 = secondStation?.get("stNm")?.asString ?: ""
        val arrmsg1_2 = secondStation?.get("arrmsg1")?.asString ?: ""

        var busData = busParcel(rtNm ?: "", stNm1 ?: "", arrmsg1 ?: "");
        // 시간따라 세팅
        if (isTimeBetween9AMAnd2PM()) {
            busData = busParcel(rtNm ?: "", stNm2 ?: "", arrmsg1_2 ?: "");
        }
        sharedHandler.setTileData(busData)
        updateTile();
    }

    override fun onBusStationDataSend() {
        Log.d("MainTileService", "onBusStationDataSend")
    }

    override fun onBackServiceFlag(flag: Boolean) {
        Log.d("MainTileService", "onBackServiceFlag")
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

// 로딩 상태 레이아웃 생성 함수

private fun tileLayout(
    context: Context,
    updateData: String = ""
): LayoutElementBuilders.LayoutElement {
    var myData = busParcel("마포06", "조회실패", "집에못간다구링");
    if (updateData != "noData") {
        myData = Gson().fromJson(updateData, busParcel::class.java)
    }

    val busStopName = Text.Builder(context, myData.stationNm)
        .setColor(argb(Colors.DEFAULT.onSurface))
        .setTypography(Typography.TYPOGRAPHY_BODY1)
        .setMultilineAlignment(LayoutElementBuilders.TEXT_ALIGN_CENTER)
        .build()

    val busArrivalTime = Text.Builder(context, myData.arrivalTime)
        .setColor(argb(Colors.DEFAULT.primary))
        .setTypography(Typography.TYPOGRAPHY_TITLE2)
        .setMultilineAlignment(LayoutElementBuilders.TEXT_ALIGN_CENTER)
        .build()

    val busName = Text.Builder(context, myData.busNm)
        .setColor(argb(Colors.DEFAULT.onSurface))
        .setTypography(Typography.TYPOGRAPHY_BODY1)
        .setMultilineAlignment(LayoutElementBuilders.TEXT_ALIGN_CENTER)
        .build()

//

    // 앱으로로 이동
    val appToAction = ActionBuilders.AndroidActivity.Builder()
        .setPackageName("com.example.home_project")
        .setClassName("com.example.home_project.presentation.MainActivity")
        .build();

    // 배경 클릭 이벤트 추가
    val backgroundClickable = ModifiersBuilders.Clickable.Builder()
        .setId("background_click")
        .setOnClick(
            ActionBuilders.LaunchAction.Builder()
                .setAndroidActivity(appToAction)
                .build()
        )
        .build()

    // 배경클릭 modifiers
    val backModifiers = ModifiersBuilders.Modifiers.Builder()
        .setClickable(backgroundClickable) // 배경 클릭 이벤트 연결
        .build()


    return PrimaryLayout.Builder(buildDeviceParameters(context.resources))
        .setContent(
            LayoutElementBuilders.Box.Builder()
                .setModifiers(backModifiers)
                .addContent(
                    LayoutElementBuilders.Column.Builder()
                        .addContent(busStopName)
                        .addContent(busArrivalTime)
                        .addContent(busName)
                        .build()
                )
                .build()
        )
        .build()
}

@Preview(
    device = Devices.WEAR_OS_LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun TilePreview() {
    LayoutRootPreview(root = tileLayout(LocalContext.current))
}