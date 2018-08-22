package com.cetcme.xkterminal.Fragment.setting;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.cetcme.xkterminal.widget.Satellite;

import java.util.List;

public class SatelliteView extends View {


    private Paint mCicrlPaint;
    private Paint mFontPaint;
    private Paint mGPSPaint;
    private Paint mBDPaint;
    private Paint mNoPaint;

    private List<Satellite> datas;


    public SatelliteView(Context context) {
        super(context);
        init();
    }

    public SatelliteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SatelliteView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SatelliteView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private void init() {
        mCicrlPaint = new Paint();
        mCicrlPaint.setAntiAlias(true);
        mCicrlPaint.setDither(true);
        mCicrlPaint.setStrokeWidth(3f);
        mCicrlPaint.setColor(Color.CYAN);
        mCicrlPaint.setStyle(Paint.Style.STROKE);

        mFontPaint = new Paint();
        mFontPaint.setAntiAlias(true);
        mFontPaint.setDither(true);
        mFontPaint.setTextSize(16f);
        mFontPaint.setTextAlign(Paint.Align.CENTER);
        mFontPaint.setColor(Color.BLACK);

        mGPSPaint = new Paint();
        mGPSPaint.setAntiAlias(true);
        mGPSPaint.setDither(true);
        mGPSPaint.setColor(Color.GREEN);

        mBDPaint = new Paint();
        mBDPaint.setAntiAlias(true);
        mBDPaint.setDither(true);
        mBDPaint.setColor(Color.RED);


        mNoPaint = new Paint();
        mNoPaint.setAntiAlias(true);
        mNoPaint.setDither(true);
        mNoPaint.setTextSize(12f);
        mNoPaint.setTextAlign(Paint.Align.CENTER);
        mNoPaint.setColor(Color.BLACK);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int height = canvas.getHeight();
        //1.画背景
        int radius = (height - 100) / 2;
        int x = height / 2 + 25;
        int y = height / 2;
        int redundancy = 10;// 冗余长度
        canvas.drawCircle(x, y, radius, mCicrlPaint);
        canvas.drawCircle(x, y, radius / 2, mCicrlPaint);

        canvas.drawLine(x - radius - redundancy, y, x + radius + redundancy, y, mCicrlPaint);// 横线
        canvas.drawLine(x, y - radius - redundancy, x, y + radius + redundancy, mCicrlPaint);// 竖线


        canvas.drawText("东", x + radius + redundancy + 16, y + 4, mFontPaint);
        canvas.drawText("西", x - radius - redundancy - 16, y + 4, mFontPaint);

        canvas.drawText("南", x, y + radius + redundancy + 16, mFontPaint);
        canvas.drawText("北", x, y - radius - redundancy - 8, mFontPaint);


        if (datas != null && !datas.isEmpty()) {
            // 卫星数据不空，画卫星
            for (Satellite satellite : datas) {
                if (satellite != null) {
                    calcSatellite(canvas, satellite, x, y, radius, satellite.getSatelliteType());
                }
            }
        }
    }


    private void calcSatellite(Canvas canvas, Satellite satellite, int cx, int cy, int r, String type) {
        if (satellite == null) return;
        /**
         * GPS卫星导航仪通常选用仰角大于5º，小于85º。 因为当卫星仰角大于85º时，L1波段的电离层折射误差较大，故规定仰角大于85º时，
         * 定位无效，不进行数据更新。而卫星仰角越小，则对流层折射误差越大，故一般选用仰角大于5º的卫星来定位。
         */
        //得到仰角
        float elevation = Float.valueOf(satellite.getElevationAngle());
        //通过仰角，计算出这个卫星应该绘制到离圆心多远的位置，这 里用的是角度的比值
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
        double radian = degreeToRadian(360 - azimuth + 90);

        double x = cx + Math.cos(radian) * r2;
        double y = cy + Math.sin(radian) * r2;
        if ("GPS".equals(type)) {
            canvas.drawCircle((float) x, (float) y, 10, mGPSPaint);
        } else if ("BD2".equals(type)) {
            canvas.drawCircle((float) x, (float) y, 10, mBDPaint);
        } else {
            return;
        }
        canvas.drawText(ltTen(satellite.getNo()), (float)x, (float)y + 5,mNoPaint);
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

    public void setDatas(List<Satellite> mDatas) {
        this.datas = mDatas;
        postInvalidate();
    }

    private String ltTen(int i){
        if (i < 10) return "0" + i;
        return "" + i;
    }
}
