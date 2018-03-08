package com.cetcme.xkterminal.MyClass;

import android.app.Activity;
import android.content.Context;

/**
 * Created by qiuhong on 15/01/2018.
 */

public class CommonUtil {

    public static int getTotalPage(int total, int perPage) {
        return (int) Math.ceil((float) total / perPage);
    }

    public static int getCountPerPage(Context context, Activity activity) {
        int screenHeight = DensityUtil.getScreenHeight(context, activity);
        int messageListHeight = screenHeight - 60 - 60 - 50 - 50; // gps 34 bottom 40 title 30 head 30
        return messageListHeight / 50  + 1;
    }

}
