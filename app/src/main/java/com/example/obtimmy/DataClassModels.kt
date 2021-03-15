package com.example.obtimmy

import com.google.gson.annotations.SerializedName

data class ApiData(val dateInformation : List<apiDays>)

data class apiDays(val dagar : List<apiDateInfo>)

data class apiDateInfo(val datum : String, val veckodag: String,
                       @SerializedName("röd dag") val rodDag: String, val helgdag : String?)

data class shiftsList(val shifts : MutableList<SingleShift>)

enum class dayType {
    notHoliday, holidayEve, holidayDay
}