package com.example.home_project.parcel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class busParcel(val busNm: String, val stationNm: String, val arrivalTime:String ) : Parcelable
