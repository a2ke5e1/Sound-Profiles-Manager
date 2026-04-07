package com.a3.soundprofiles.data.local

import androidx.room.TypeConverter
import com.a3.soundprofiles.data.local.entities.DAY
import java.util.Date

class Converters {
  @TypeConverter
  fun fromTimestamp(value: Long?): Date? {
    return value?.let { Date(it) }
  }

  @TypeConverter
  fun dateToTimestamp(date: Date?): Long? {
    return date?.time // Room saves this as a Long
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
    if (value.isEmpty()) {
      return emptyList()
    }
    return value.split(",").map { DAY.valueOf(it) }
  }

  @TypeConverter
  fun dayListToString(dayList: List<DAY>): String {
    return dayList.joinToString(",")
  }
}
