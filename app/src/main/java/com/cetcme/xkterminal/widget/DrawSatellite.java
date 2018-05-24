package com.cetcme.xkterminal.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.cetcme.xkterminal.R;

import java.util.ArrayList;

/**
 * Created by dell on 2018/5/24.
 */

public class DrawSatellite extends View {
    private int mSatelliteX;
    private int mSateLliteY;
    private Context mContext;

    private ArrayList<Satellite> mSatelliteList;
    private Bitmap mSatelliteBitmap;
    private Paint p;

    public DrawSatellite(Context context,int x,int y,ArrayList<Satellite> list) {
        super(context);
        mContext = context;
        this.mSatelliteX = x;
        this.mSateLliteY = y;
        this.mSatelliteList = list;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.RED);
        p.setTextSize(20);

        drawCircle(canvas,p);

    }

    private void drawCircle(Canvas canvas,Paint p){
        mSatelliteBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_satellite_blue);
        for(Satellite s : mSatelliteList){
            drawSatellite(canvas, s, mSatelliteX, mSateLliteY, mSatelliteX);
        }
    }


    /**
     * 在背景罗盘上绘制卫星
     * @param canvas
     * @param satellite
     * @param cx  中心圆点的X座标
     * @param cy  中心圆点的Y座标
     * @param r   罗盘背景的半径
     */
    private void drawSatellite(Canvas canvas, Satellite satellite, int cx, int cy, int r) {

        /**
         * GPS卫星导航仪通常选用仰角大于5º，小于85º。 因为当卫星仰角大于85º时，L1波段的电离层折射误差较大，故规定仰角大于85º时，
         * 定位无效，不进行数据更新。而卫星仰角越小，则对流层折射误差越大，故一般选用仰角大于5º的卫星来定位。
         */
        //得到仰角
        float elevation = Float.valueOf(satellite.getElevationAngle());
        //通过仰角，计算出这个卫星应该绘制到离圆心多远的位置，这里用的是角度的比值
        double r2 = r * ((90.0f - elevation) / 90.0f);

        /*得到方位角（与正北向也就是Y轴顺时针方向的夹角，注意我们通常几何上的角度
         * 是与X轴正向的逆时针方向的夹角）,在计算X，Y座标的三角函数时，要做转换
         */
        double azimuth = Float.valueOf(satellite.getAzimuth());

        /*
         * 转换成XY座标系中的夹角,方位角是与正北向也就是Y轴顺时针方向的夹角，
         * 注意我们通常几何上的角度是与X轴正向的逆时针方向的夹角）,
         * 在计算X，Y座标的三角函数时，要做转换
         */
        double radian = degreeToRadian(360-azimuth + 90);

        double x = cx + Math.cos(radian) * r2;
        double y = cy + Math.sin(radian) * r2;

        //得到卫星图标的半径
        int sr = mSatelliteBitmap.getWidth() / 2;
        //以x,y为中心绘制卫星图标
        canvas.drawBitmap(mSatelliteBitmap, (float) (x - sr), (float) (y - sr),p);
        //在卫星图标的位置上绘出文字（卫星编号及信号强度）
        int snr=(int)satellite.getNum();
//        int signLevel=snrToSignalLevel(snr);  //暂时不用
//      String info = String.format("#%s_%s", satellite.getNum(), snr);
        String info = String.valueOf(satellite.getNum());
        canvas.drawText(info, (float) (x)-15, (float) (y)+5, p);

    }

    /**
     * 将角度转换为弧度，以用于三角函数的运算
     *
     * @param degree
     * @return
     */
    private double degreeToRadian(double degree) {
        return (degree * Math.PI) / 180.0d;
    }

}
