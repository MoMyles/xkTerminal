package com.cetcme.xkterminal.MyClass;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by qiuhong on 15/01/2018.
 */

public class CommonUtil {

    public static int getTotalPage(long total, int perPage) {
        return (int) Math.ceil((float) total / perPage);
    }

    public static int getCountPerPage(Context context, Activity activity) {
        int screenHeight = DensityUtil.getScreenHeight(context, activity);
        int messageListHeight = screenHeight - 60 - 60 - 50 - 50; // gps 34 bottom 40 title 30 head 30
        return messageListHeight / 50  + 1;
    }

    /**
     * int[] 里是否包含 targetValue
     */
    public static boolean useLoop(int[] arr, int targetValue) {
        for (int s : arr) {
            if (s == targetValue)
                return true;
        }
        return false;
    }


    /**
     * 是否全是数字
     * @param txt
     * @return
     */
    public static boolean isNumber(String txt) {
        Pattern p = Pattern.compile("[0-9]*");
        Matcher m = p.matcher(txt);
        return m.matches();
    }
}
