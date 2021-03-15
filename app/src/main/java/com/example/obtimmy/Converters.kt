package com.example.obtimmy

import androidx.room.TypeConverter


class Converters {

    @TypeConverter
    fun toDayType(value: String) = enumValueOf<dayType>(value)

    @TypeConverter
    fun fromDayType(value: dayType) = value.name

}