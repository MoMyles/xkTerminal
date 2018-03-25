package com.cetcme.xkterminal.MyClass;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by qiuhong on 12/01/2018.
 */

public class DateUtil {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String Date2String(Date date) {
        return sdf.format(date);
    }

    public static String Date2String(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    public static Date String2Date(String dateString) {

        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date String2Date(String dateString, String format) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(format);

        Date date = null;
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String modifyDate(String dateStr) {
        Date date = new Date(dateStr);
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
        if (now.getYear() != date.getYear()) {
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        } else if (now.getMonth() == date.getMonth() && now.getDate() == date.getDate()) {
            sdf = new SimpleDateFormat("HH:mm");
        }

        return  sdf.format(date);
    }
}
