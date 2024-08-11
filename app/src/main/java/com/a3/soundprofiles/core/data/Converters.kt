package com.a3.soundprofiles.core.data

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long): Date {
        return Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun fromDay(value: String): DAY {
        return DAY.valueOf(value)
    }

    @TypeConverter
    fun dayToString(day: DAY): String {
        return day.name
    }

    @TypeConverter
    fun fromDayList(value: String): List<DAY> {
        return value.split(",").map { DAY.valueOf(it) }
    }

    @TypeConverter
    fun dayListToString(dayList: List<DAY>): String {
        return dayList.joinToString(",")
    }
}
