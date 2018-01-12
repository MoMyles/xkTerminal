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

    public static Date String2Date(String dateString) {

        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
