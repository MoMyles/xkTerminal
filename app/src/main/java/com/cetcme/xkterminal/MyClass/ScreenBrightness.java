package com.cetcme.xkterminal.MyClass;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.view.WindowManager;

/**
 * Created by qiuhong on 13/03/2018.
 */

public class ScreenBrightness {

    /**
     * 判断是否开启了自动亮度调节
     */
    public static boolean isAutoBrightness(Context context) {
        ContentResolver resolver = context.getContentResolver();
        boolean autoBrightness = false;

        try {
            autoBrightness = Settings.System.getInt(resolver,

            Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        return autoBrightness;
    }

    /**
     * 获取屏幕的亮度
     */
    public static int getScreenBrightness(Context context) {
        int nowBrightnessValue = 0;
        ContentResolver resolver = context.getContentResolver();
        try {
            nowBrightnessValue = android.provider.Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nowBrightnessValue;
    }

    /**
     * 设置当前Activity显示时的亮度
     * 屏幕亮度最大数值一般为255，各款手机有所不同
     * screenBrightness 的取值范围在[0,1]之间
     */
    public static void setBrightness(Activity activity, int brightness) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
        activity.getWindow().setAttributes(lp);
    }


    /**
     * 开启关闭自动亮度调节
     */
    public static boolean autoBrightness(Context activity, boolean flag) {
        int value = 0;
        if (flag) {
            value = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;  //开启
        } else {
            value = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;//关闭
        }
        return Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                value);
    }


    /**
     * 保存亮度设置状态，退出app也能保持设置状态
     */
    public static void saveBrightness(Context context, int brightness) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = android.provider.Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        android.provider.Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        resolver.notifyChange(uri, null);
    }

    /**
     * 循环增加亮度
     */
    public static void modifyBrightness(Activity activity) {
        int bright = getScreenBrightness(activity);
        int newBright = bright + 15;
        if (newBright > 255) newBright = 170;
//        setBrightness(activity, newBright);
        saveBrightness(activity, newBright);
    }

}
