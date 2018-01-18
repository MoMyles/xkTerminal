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
        int messageListHeight = screenHeight - 60 - 50 - 60 - 50; // gps 60 bottom 60 title 50
        return messageListHeight / 51;
    }

}
