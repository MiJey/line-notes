package dev.mijey.linenotes

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateHelper {
    fun dateString(timestamp: Long): String {
        val date = Date(timestamp)
        return if (isToday(date)) {
            SimpleDateFormat("HH:mm").format(timestamp)
        } else {
            DateFormat.getDateInstance(DateFormat.LONG).format(date)
        }
    }

    fun isToday(date: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date
        val cal2 = Calendar.getInstance()
        cal2.time = Date(System.currentTimeMillis())

        return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}