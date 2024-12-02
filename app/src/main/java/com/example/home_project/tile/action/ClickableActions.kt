package com.example.home_project.tile.action

import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ModifiersBuilders

class ClickableActions {
    internal fun launchActivityClickable(
        clickableId: String,
        androidActivity: ActionBuilders.AndroidActivity
    ) = ModifiersBuilders.Clickable.Builder()
        .setId(clickableId)
        .setOnClick(
            ActionBuilders.LaunchAction.Builder()
                .setAndroidActivity(androidActivity)
                .build()
        )
        .build()
}