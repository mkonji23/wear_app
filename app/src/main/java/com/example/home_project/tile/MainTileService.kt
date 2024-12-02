package com.example.home_project.tile

import android.content.Context
import android.content.IntentFilter
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.StateBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.expression.StateEntryBuilders
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.EventBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.example.home_project.broadcast.MyReceiver
import com.example.home_project.R
import com.example.home_project.broadcast.sender.BroadCastSender
import com.example.home_project.parcel.busParcel
import com.example.home_project.sharedPreference.SharedHandler


import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.compose.tools.buildDeviceParameters
import com.google.android.horologist.tiles.SuspendingTileService
import com.google.gson.Gson

private var RESOURCES_VERSION = 1

/**
 * Skeleton for a tile with no images.
 */
@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {
    private val myRceiver = MyReceiver()
    private val mySender = BroadCastSender()
    private val updateData = "test"
    private val sharedHandler = SharedHandler(this)

    override fun onTileAddEvent(requestParams: EventBuilders.TileAddEvent) {
        super.onTileAddEvent(requestParams)
        // 동적으로 BroadcastReceiver 등록
        val intentFilter = IntentFilter("MY_ACTION")
        registerReceiver(myRceiver, intentFilter)
        Log.d("MainTileService", "Receiver registered")

    }

    override fun onTileRemoveEvent(requestParams: EventBuilders.TileRemoveEvent) {
        super.onTileRemoveEvent(requestParams)
        // 등록 해제
        unregisterReceiver(myRceiver)
    }

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder().setVersion(RESOURCES_VERSION.toString())
            .addIdToImageMapping(
                "tree", // 리소스 ID
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                            .setResourceId(R.drawable.tree)
                            .build()
                    )
                    .build()
            )
            .build()
    }

    override fun onTileLeaveEvent(requestParams: EventBuilders.TileLeaveEvent) {
        super.onTileLeaveEvent(requestParams)
    }

    override fun onTileEnterEvent(requestParams: EventBuilders.TileEnterEvent) {
        super.onTileEnterEvent(requestParams)
        // 진입시 화면 업데이트
        getUpdater(this).requestUpdate(MainTileService::class.java)
    }

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        RESOURCES_VERSION++
        val lastClickableId = requestParams.currentState.lastClickableId
        val sharedData = sharedHandler.getTileData();
        Log.d("tileRequest", "Last Clickable ID: $lastClickableId")
        Log.d("tileRequest", "Updated Data: $updateData")

        // 타일 갱신이 필요한 경우
        if (lastClickableId == "refresh") {
            mySender.sendBroadcastRequest(this,"MY_BUS_WATCH")
            Log.d("tileRequest", "Data after refresh: $updateData")
        }

        val multiTileTimeline = TimelineBuilders.Timeline.fromLayoutElement(
            when (requestParams.currentState.lastClickableId) {
                "foo" -> tileLayout(this, sharedData)
                else -> tileLayout(this, sharedData)
            }
        )
        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION.toString())  // 고유 버전 생성
            .setTileTimeline(multiTileTimeline)
            .build()
    }
}

private fun tileLayout(context: Context, updateData: String = ""): LayoutElementBuilders.LayoutElement {
    val myData = Gson().fromJson(updateData, busParcel::class.java)
    val busStopName = Text.Builder(context, myData.stationNm)
        .setColor(argb(Colors.DEFAULT.onSurface))
        .setTypography(Typography.TYPOGRAPHY_CAPTION1)
        .setMultilineAlignment(LayoutElementBuilders.TEXT_ALIGN_CENTER)
        .build()

    val busArrivalTime = Text.Builder(context, myData.arrivalTime)
        .setColor(argb(Colors.DEFAULT.primary))
        .setTypography(Typography.TYPOGRAPHY_BODY2)
        .setMultilineAlignment(LayoutElementBuilders.TEXT_ALIGN_CENTER)
        .build()

    val busName = Text.Builder(context, myData.busNm)
        .setColor(argb(Colors.DEFAULT.onSurface))
        .setTypography(Typography.TYPOGRAPHY_CAPTION2)
        .setMultilineAlignment(LayoutElementBuilders.TEXT_ALIGN_CENTER)
        .build()

//
    val buttonTest =  Button.Builder(context, ModifiersBuilders.Clickable.Builder()
        .setId("refresh")
        .setOnClick(
            ActionBuilders.LoadAction.Builder()
                .setRequestState(
                    StateBuilders.State.Builder()
                        .addIdToValueMapping(
                            "busArrivalTime",
                            StateEntryBuilders.StateEntryValue.fromString(updateData)
                        )
                        .build()
                ).build()
        ).build())
        .setTextContent("refresh")
        .build()

    // 앱으로로 이동
    val appToAction = ActionBuilders.AndroidActivity.Builder()
        .setPackageName("com.example.home_project")
        .setClassName("com.example.home_project.MainActivity")
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
//                        .addContent(buttonTest)
                        .build()
                )
                .build()
        )
        .build()
}



@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun TilePreview() {
    LayoutRootPreview(root = tileLayout(LocalContext.current))
}