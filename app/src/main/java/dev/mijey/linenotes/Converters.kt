package dev.mijey.linenotes

import androidx.room.TypeConverter
import java.lang.Exception

class Converters {
    @TypeConverter
    fun toArrayList(value: String): ArrayList<String> {
        return try {
            ArrayList(value.split(","))
        } catch (e: Exception) {
            ArrayList()
        }
    }

    @TypeConverter
    fun toString(list: ArrayList<String>): String {
        var result = ""

        if (list.isNotEmpty()) {
            for (i in 0 until list.size - 1) {
                result += "${list[i]},"
            }
            result += list[list.size - 1]
        }

        return result
    }
}