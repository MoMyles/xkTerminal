package com.cetcme.xkterminal.MyClass;

/**
 * Created by qiuhong on 9/1/16.
 */
import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

public class DensityUtil {

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 获取屏幕宽度（dp）
     */
    public static int getScreenWidth(Context context, Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

        return DensityUtil.px2dip(context, (float) dm.widthPixels);
    }

    /**
     * 获取屏幕高度（dp）
     */
    public static int getScreenHeight(Context context, Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

        return DensityUtil.px2dip(context, (float) dm.heightPixels);
    }
}
