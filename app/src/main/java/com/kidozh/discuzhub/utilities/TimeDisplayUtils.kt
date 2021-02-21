package com.kidozh.discuzhub.utilities

import android.content.Context
import android.util.Log
import com.kidozh.discuzhub.R
import java.text.DateFormat
import java.util.*

class TimeDisplayUtils(private val mContext: Context) {
    companion object {
        private val TAG = TimeDisplayUtils::class.java.simpleName
        fun dateToCalendar(date: Date?): Calendar {
            val calendar = Calendar.getInstance()
            calendar.time = date
            return calendar
        }

        @JvmStatic
        fun getLocalePastTimeString(mContext: Context, date: Date): String {
            val now = Date()
            val nowCalendar = Calendar.getInstance()
            val dateCalendar = dateToCalendar(date)
            val nowDay = nowCalendar[Calendar.DAY_OF_YEAR]
            val dateDay = dateCalendar[Calendar.DAY_OF_YEAR]
            val nowYear = nowCalendar[Calendar.YEAR]
            val dateYear = dateCalendar[Calendar.YEAR]
            val timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
            val timeString = timeFormat.format(date)
            var timeMillsecondsInterval = now.time - date.time
            // handle ago
            return if (timeMillsecondsInterval >= 0) {
                if (timeMillsecondsInterval < 1000 * 60) {
                    mContext.getString(R.string.date_just_now)
                } else if (timeMillsecondsInterval < 1000 * 60 * 60) {
                    String.format(mContext.getString(R.string.date_minutes_before), (timeMillsecondsInterval.toInt() / (60 * 1000)).toString())
                } else if (timeMillsecondsInterval < 1000 * 60 * 60 * 24) {
                    String.format(mContext.getString(R.string.date_hours_before), (timeMillsecondsInterval.toInt() / (60 * 60 * 1000)).toString())
                } else if (nowDay == dateDay) {
                    val timeTemplate = mContext.getString(R.string.time_template_today_time)
                    String.format(timeTemplate, timeString)
                } else if (nowDay - dateDay > 0 && nowYear == dateYear) {
                    val intervalDay = nowDay - dateDay
                    if (intervalDay == 1) {
                        val timeTemplate = mContext.getString(R.string.time_template_yesterday_time)
                        String.format(timeTemplate, timeString)
                    } else if (intervalDay < 10) {
                        val timeTemplate = mContext.getString(R.string.time_template_days_ago)
                        String.format(timeTemplate, intervalDay, timeString)
                    } else {
                        val df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault())
                        df.format(date)
                    }
                } else {
                    val df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault())
                    df.format(date)
                }
            } else {
                // future
                // reverse it
                timeMillsecondsInterval = -timeMillsecondsInterval
                Log.d(TAG, "Future time $timeMillsecondsInterval")
                if (timeMillsecondsInterval < 1000 * 60) {
                    mContext.getString(R.string.date_right_away)
                } else if (timeMillsecondsInterval < 1000 * 60 * 60) {
                    mContext.getString(R.string.date_in_minutes, timeMillsecondsInterval.toInt() / (60 * 1000))
                } else if (timeMillsecondsInterval < 1000 * 60 * 60 * 24) {
                    mContext.getString(R.string.date_in_hours, timeMillsecondsInterval.toInt() / (60 * 60 * 1000))
                } else if (nowDay == dateDay) {
                    val timeTemplate = mContext.getString(R.string.time_template_today_time)
                    String.format(timeTemplate, timeString)
                } else if (dateDay - nowDay > 0 && nowYear == dateYear) {
                    val intervalDay = dateDay - nowDay
                    if (intervalDay == 1) {
                        mContext.getString(R.string.date_tomorrow_time_template, timeString)
                    } else if (intervalDay < 10) {
                        mContext.getString(R.string.date_in_days, intervalDay, timeString)
                    } else {
                        val df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault())
                        df.format(date)
                    }
                } else {
                    val df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault())
                    df.format(date)
                }
            }
        }
    }
}