package com.cetcme.xkterminal.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by dell on 2018/5/24.
 */

public class DrawEarth extends View {
    private float mSatelliteX;
    private float mSateLliteY;

    public DrawEarth(Context context, float x, float y) {
        super(context);
        this.mSatelliteX = x;
        this.mSateLliteY = y;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.BLACK);
        p.setTextSize(25);

        canvas.drawCircle(mSatelliteX, mSateLliteY, mSatelliteX * 1 / 3, p);
        canvas.drawCircle(mSatelliteX, mSateLliteY, mSatelliteX * 2 / 3, p);
        canvas.drawCircle(mSatelliteX, mSateLliteY, mSatelliteX, p);

        canvas.drawLine(0, mSateLliteY, mSatelliteX * 2, mSateLliteY, p);//X
        canvas.drawLine(mSatelliteX, 0, mSatelliteX, mSateLliteY * 2, p);//Y

        p.setStyle(Paint.Style.FILL);
        canvas.drawText("北", mSatelliteX - 15, 0 + 25, p);
        canvas.drawText("南", mSatelliteX - 15, mSateLliteY * 2, p);
        canvas.drawText("西", 0, mSateLliteY + 10, p);
        canvas.drawText("东", mSatelliteX * 2 - 25, mSateLliteY + 10, p);

    }

}
