package com.cetcme.xkterminal.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class ImageUtil {
    public static Bitmap base64(String str) {
        byte[] byteArr = Base64.decode(str.split(",")[1], Base64.DEFAULT);
        Bitmap bm = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.length);
        return bm;
    }
}
