package com.cetcme.xkterminal.MyClass;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ScreenUtil {

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        if (wm != null && wm.getDefaultDisplay() != null) {
            wm.getDefaultDisplay().getMetrics(dm);
            int width = dm.widthPixels;         // 屏幕宽度（像素）
            int height = dm.heightPixels;       // 屏幕高度（像素）
            float density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
            int densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
            // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
            int screenWidth = (int) (width / density);  // 屏幕宽度(dp)
            int screenHeight = (int) (height / density);// 屏幕高度(dp)
            return screenWidth;
        } else {
            return 0;
        }
    }

    public static int getScreenHigh(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        if (wm != null && wm.getDefaultDisplay() != null) {
            wm.getDefaultDisplay().getMetrics(dm);
            int width = dm.widthPixels;         // 屏幕宽度（像素）
            int height = dm.heightPixels;       // 屏幕高度（像素）
            float density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
            int densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
            // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
            int screenWidth = (int) (width / density);  // 屏幕宽度(dp)
            int screenHeight = (int) (height / density);// 屏幕高度(dp)
            return screenHeight;
        } else {
            return 0;
        }
    }


    /**
     * 获取屏幕参数
     */
    public static void getAndroidScreenProperty(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;         // 屏幕宽度（像素）
        int height = dm.heightPixels;       // 屏幕高度（像素）
        float density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
        // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
        int screenWidth = (int) (width / density);  // 屏幕宽度(dp)
        int screenHeight = (int) (height / density);// 屏幕高度(dp)

        System.out.println("屏幕宽度（像素）：" + width);
        System.out.println("屏幕高度（像素）：" + height);
        System.out.println("屏幕密度（0.75 / 1.0 / 1.5）：" + density);
        System.out.println("屏幕密度dpi（120 / 160 / 240）：" + densityDpi);
        System.out.println("屏幕宽度（dp）：" + screenWidth);
        System.out.println("屏幕高度（dp）：" + screenHeight);
    }
}
