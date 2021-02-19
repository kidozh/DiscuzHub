package com.kidozh.discuzhub.utilities;

import android.content.Context;
import android.util.Log;


import com.kidozh.discuzhub.R;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class timeDisplayUtils {
    private static String TAG = timeDisplayUtils.class.getSimpleName();
    private Context mContext;
    public timeDisplayUtils(Context mContext){
        this.mContext = mContext;
    }

    public static Calendar dateToCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static String getLocalePastTimeString(Context mContext,Date date){
        Date now = new Date();
        Calendar nowCalendar = Calendar.getInstance();
        Calendar dateCalendar = dateToCalendar(date);
        int nowDay = nowCalendar.get(Calendar.DAY_OF_YEAR);
        int dateDay = dateCalendar.get(Calendar.DAY_OF_YEAR);
        int nowYear = nowCalendar.get(Calendar.YEAR);
        int dateYear = dateCalendar.get(Calendar.YEAR);
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
        String timeString = timeFormat.format(date);
        long timeMillsecondsInterval = now.getTime() - date.getTime();
        // handle ago
        if(timeMillsecondsInterval >= 0){
            if(timeMillsecondsInterval < 1000* 60 ){
                return mContext.getString(R.string.date_just_now);
            }
            else if (timeMillsecondsInterval < 1000* 60 * 60 ){
                return String.format(mContext.getString(R.string.date_minutes_before), String.valueOf((int) timeMillsecondsInterval/(60*1000)));
            }
            else if (timeMillsecondsInterval < 1000* 60 * 60 * 24 ){
                return String.format(mContext.getString(R.string.date_hours_before), String.valueOf((int) timeMillsecondsInterval/(60*60*1000)));
            }
            else if(nowDay == dateDay){

                String timeTemplate = mContext.getString(R.string.time_template_today_time);

                return String.format(timeTemplate,timeString);
            }
            else if(nowDay - dateDay > 0 && nowYear == dateYear) {
                int intervalDay = nowDay - dateDay;
                if(intervalDay == 1){
                    String timeTemplate = mContext.getString(R.string.time_template_yesterday_time);
                    return String.format(timeTemplate,timeString);
                }
                else if(intervalDay < 10) {
                    String timeTemplate = mContext.getString(R.string.time_template_days_ago);
                    return String.format(timeTemplate,intervalDay,timeString);
                }
                else {
                    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault());
                    return df.format(date);
                }

            }
            else {
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault());
                return df.format(date);
            }
        }
        else {
            // future
            // reverse it
            timeMillsecondsInterval = -timeMillsecondsInterval;
            Log.d(TAG,"Future time " + timeMillsecondsInterval);
            if(timeMillsecondsInterval < 1000* 60 ){
                return mContext.getString(R.string.date_right_away);
            }
            else if (timeMillsecondsInterval < 1000* 60 * 60 ){
                return mContext.getString(R.string.date_in_minutes,(int) timeMillsecondsInterval/(60*1000));
            }
            else if (timeMillsecondsInterval < 1000* 60 * 60 * 24 ){
                return mContext.getString(R.string.date_in_hours,(int) timeMillsecondsInterval/(60*60*1000));
            }
            else if(nowDay == dateDay){

                String timeTemplate = mContext.getString(R.string.time_template_today_time);

                return String.format(timeTemplate,timeString);
            }
            else if(dateDay - nowDay > 0 && nowYear == dateYear) {
                int intervalDay = dateDay - nowDay;
                if(intervalDay == 1){
                    return mContext.getString(R.string.date_tomorrow_time_template,timeString);
                }
                else if(intervalDay < 10) {
                    return mContext.getString(R.string.date_in_days,intervalDay,timeString);
                }
                else {
                    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault());
                    return df.format(date);
                }

            }
            else {
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault());
                return df.format(date);
            }
        }

    }
}
