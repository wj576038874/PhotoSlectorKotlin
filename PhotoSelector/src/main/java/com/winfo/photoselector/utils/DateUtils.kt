package com.winfo.photoselector.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun getImageTime(time: Long): String {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        val imageTime = Calendar.getInstance()
        imageTime.timeInMillis = time
        if (sameDay(calendar, imageTime)) {
            return "今天"
        } else if (sameWeek(calendar, imageTime)) {
            return "本周"
        } else if (sameMonth(calendar, imageTime)) {
            return "本月"
        } else {
            val date = Date(time)
            val sdf = SimpleDateFormat("yyyy-MM", Locale.CANADA)
            return sdf.format(date)
        }
    }

    fun sameDay(calendar1: Calendar, calendar2: Calendar): Boolean {
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) && calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
    }

    fun sameWeek(calendar1: Calendar, calendar2: Calendar): Boolean {
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) && calendar1.get(Calendar.WEEK_OF_YEAR) == calendar2.get(Calendar.WEEK_OF_YEAR)
    }

    fun sameMonth(calendar1: Calendar, calendar2: Calendar): Boolean {
        return calendar1.get(java.util.Calendar.YEAR) == calendar2.get(java.util.Calendar.YEAR) && calendar1.get(java.util.Calendar.MONTH) == calendar2.get(java.util.Calendar.MONTH)
    }
}