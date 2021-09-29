package com.raywenderlich.podplay.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun jsonDateToShortDate(jsonDate: String?): String {
        //checking if jsonDate is not null, if null return hyphen
        //indicating no date was provided
        if (jsonDate == null) {
            return "-"
        }
        //defining a SimpleDateFormat to match date format returned by iTunes
        val inFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
            Locale.getDefault())
        //parsing jsonDate string & placing it into a Date object named date
        val date = inFormat.parse(jsonDate) ?: return "-"
        //defining output format as a short date to match currently defined locale
        val outputFormat =
            DateFormat.getDateInstance(DateFormat.SHORT,
                Locale.getDefault())
        //returning formatted date
        return outputFormat.format(date)
    }
}