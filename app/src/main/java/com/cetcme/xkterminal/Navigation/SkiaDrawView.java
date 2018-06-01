package com.cetcme.xkterminal.Navigation;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.cetcme.xkterminal.Sqlite.Bean.LocationBean;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import yimamapapi.skia.M_POINT;
import yimamapapi.skia.OtherShipBaicInfo;
import yimamapapi.skia.OtherVesselCurrentInfo;
import yimamapapi.skia.YimaLib;

public class SkiaDrawView extends View {

    //机轮底渔区禁拖网线
    private static final String LINE_JIN_TUO = "124, 39.55;123.333333, 38.933333;121, 38.666667;121, 39.5;121.333333, 40;120.5, 40;119, 38.933333;119, 38.2;120, 37.833333;120.5, 38.083333;121, 38.083333;121, 38;123.05, 37.333333;122.741667, 36.802778;120.633333, 35.183333;123.416667, 30.733333;122.75, 29;121.5, 27.5;121.166667, 27";
    private static final String LINE_DALU_LINGHAI = "122.700833, 37.4;122.700833, 37.385278;122.567222, 36.952222;122.535278, 36.916944;122.516944, 36.885278;122.252222, 36.735556;120.883611, 35.885;119.900556, 35.000833;121.335556, 33.352222;121.634444, 33.0025;122.235, 31.4175;123.151111, 30.733611;123.151944, 30.718056;122.935278, 30.166944;122.268056, 28.884167;121.916667, 28.385833;121.901944, 28.384722;121.118889, 27.4525;120.501111, 26.368333;120.400833, 26.151111;119.934167, 25.418889;119.468611, 24.968333;118.233889, 24.151944;117.684167, 23.519167;117.235833, 23.2025;117.219167, 23.200833;116.485278, 22.933611;115.118056, 22.3025;113.966667, 21.801389;112.785833, 21.566944;111.267778, 19.968056;111.202222, 19.883333;110.485, 18.651944;110.483611, 18.651111;110.134444, 18.433611;110.05, 18.383333;109.700278, 18.183333;109.685556, 18.183333;109.567778, 18.151389;109.118333, 18.235;108.950278, 18.3175;108.684167, 18.500556;108.683611, 18.501111;108.668333, 18.516667;108.668056, 18.516944;108.6175, 18.834722;108.6, 19.185;108.635, 19.350278";
    private static final String LINE_XISHA = "112.733889, 16.668056;112.734722, 16.666944;112.735278, 16.652222;112.585556, 16.067778;112.535278, 16.019167;112.518889, 16.018056;112.510556, 16.016667;111.201667, 15.768056;111.200278, 15.767778;111.185556, 15.767778;111.185, 15.768056;111.184444, 15.768611;111.184167, 15.769167;111.184444, 15.783889;111.435833, 17.069167;111.435833, 17.084444;111.450556, 17.085278;111.452222, 17.1;111.483889, 17.101389;111.516667, 17.116667;111.518333, 17.116944;111.533333, 17.1025;112.235278, 16.985833;112.251667, 16.985278;112.268333, 16.984444;112.300833, 16.967778;112.318333, 16.951667;112.334722, 16.935833";
    private static final String LINE_DIAOYUDAO = "123.451388, 25.733611;123.451111, 25.733889;123.451111, 25.734444;123.451389, 25.735278;123.668611, 25.918889;123.683611, 25.918889;123.684167, 25.918333;123.684444, 25.917778;123.684444, 25.9175;123.551111, 25.717222;123.550556, 25.717222;123.451667, 25.733333;123.451389, 25.733611";
    private static final String LINE_CHIWEIYU = "124.551944, 25.9175;124.550556, 25.917222;124.550833, 25.9175;124.551389, 25.918056;124.551944, 25.918056;124.551944, 25.9175";

    private static final String LINE_BLUE1 = "108.496704268055,19.4957039147308;108.44153508047,19.2349027507802;108.5,18.5;109.046394705594,18.1032713787714;109.5,18;109.511775546856,17.9952248540181;109.745450483872,18.0570920506318;110.521709758975,18.5;111.5,20;112.435228786112,21;112.881819084948,21.4573315609717;114.062296626458,21.7019606418628;116.5,22.7990580678651;117.221140746571,23.0698212148108;117.827024400406,23.5;118.29125074926,24.059715635161;119.561046350537,24.9176241327978;120.050304512319,25.3727480042232;120.5,26.1009461985037;121.254782726736,27.414298576538;122,28.3416134645673;122.417169094356,28.8895826057635;123.022910521982,29.9939691250191;123.300536083551,30.7881602806564;122.318940166345,31.5232366752766;121.733629929959,33.0912292962786;120.076694585551,35;120.912273568246,35.757970730412;122.680429808734,36.8782865851676;122.835171925019,37.4267108502352";

    private static final String LINE_BLUE2 = "111.288387523593,17.3697313541462;110.917015168613,15.5573409075002;112.788388559325,15.8697329241532;112.84418031168,16;113,16.7772151091301;112.610929327575,17.0872682465386;111.288387523593,17.3697313541462";


    public static YimaLib mYimaLib;
    public Bitmap fSkiaBitmap;
    private int mLastX, mLastY;
    private int mCurrX, mCurrY;

    private int mFirstX, mFirstY; // 用于计算涂是否移动

    private int mLastX0, mLastY0, mLastX1, mLastY1;
    private int mCurrX0, mCurrY0, mCurrX1, mCurrY1;

    private Context mContext;

    public boolean bNormalDragMapMode; //是否使用移动贴图模式
    private boolean bDragingMap;//是否真在进行拽图
    private int dragStartPointX, dragStartPointY;//拽动的起始位置
    private int dragMapOffsetPointX, dragMapOffsetPointY; //移动拽图的X/Y偏移量

    private double pinchScaleFactor;//MotionEvent.ACTION_DOWN;比例尺变化因子
    private int pasteWidth, pasteHeight; //pinch时贴图宽度和高度
    private M_POINT scrnCenterPointGeo;

    public SkiaDrawView(Context ctx, AttributeSet attr) {
        super(ctx, attr);
        mYimaLib = new YimaLib();
        mYimaLib.Create();
        mYimaLib.Init(Constant.YIMA_WORK_PATH);//初始化，传入WorkDir初始化目录地址
        mYimaLib.SetDrawOwnShipSpecialOptions(false, true, true, 255, 0, 0);
        mContext = ctx;
        bNormalDragMapMode = false;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        fSkiaBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        String strTtfFilePath = Constant.YIMA_WORK_PATH + "/DroidSansFallback.ttf";
//        mYimaLib.SetDisplayCategory(3);
        mYimaLib.RefreshDrawer(fSkiaBitmap, strTtfFilePath);//刷新绘制器，需要传入字体文件地址，用户可以自己修改为别的字体
        mYimaLib.OverViewLibMap(0);//概览第一幅图
//        mYimaLib.SetOwnShipBasicInfo("基站-2431", "4132431", 100, 20);
//        mYimaLib.SetDisplayCategory(3);
//        mYimaLib.SetIfShowSoundingAndMinMaxSound(true, 0, 20);
    }

    @Override
    public void onDraw(Canvas canvas) {
        mYimaLib.ViewDraw(fSkiaBitmap, null, null);
        //YimaLib.ViewDraw(fSkiaBitmap, "com/example/viewdraw/viewdrawinndkskiausestaticlib/SkiaDrawView", "AfterDraw");//绘制海图到fSkiaBitmap
        canvas.drawBitmap(fSkiaBitmap, 0, 0, null);//

        createLine(canvas, Color.BLACK, LINE_JIN_TUO);
        createLine(canvas, Color.RED, LINE_DALU_LINGHAI);
        createLine(canvas, Color.RED, LINE_XISHA);
        createLine(canvas, Color.RED, LINE_DIAOYUDAO);
        createLine(canvas, Color.RED, LINE_CHIWEIYU);
        createLine(canvas, Color.BLUE, LINE_BLUE1);
        createLine(canvas, Color.BLUE, LINE_BLUE2);
//        Paint paint = new Paint();
//        paint.setARGB(255,255, 0, 0);
//        canvas.drawRect(0, 0, 500, 800, paint);

        Log.i("SkiaDrawView.onDraw", "onDraw end.");
    }

    private void createLine(Canvas canvas, int color, String line) {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(color);
        paint.setStrokeWidth(1f);

        String[] points = line.split(";");
        float[] fPoints = new float[points.length * 2];
        int index = 0;
        for (String str : points) {
            String[] point = str.split(",");
            M_POINT p = mYimaLib.getScrnPoFromGeoPo((int) (Float.parseFloat(point[0]) * 10000000), (int) (Float.parseFloat(point[1]) * 10000000));
            //Log.e("TAG", p.x + ",");
            fPoints[index++] = p.x;
            fPoints[index++] = p.y;
        }
        for (int i = 0; i < fPoints.length - 2; i += 2) {
            canvas.drawLine(fPoints[i], fPoints[i + 1], fPoints[i + 2], fPoints[i + 3], paint);
        }
    }

    private String tag = "SkiaDrawView";

    private boolean dragingMap = false;

    @Override
    //手势滑动
    public boolean onTouchEvent(MotionEvent event) {

        mCurrX = mLastX;
        mCurrY = mLastY;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                Log.i(tag, "down");
                if (onMapClickListener != null) {
                    onMapClickListener.onMapTouched(MotionEvent.ACTION_DOWN);
                }
                mLastX = (int) event.getX();
                mLastY = (int) event.getY();

                if (mFirstX == 0) mFirstX = mLastX;
                if (mFirstY == 0) mFirstY = mLastY;

                if (bNormalDragMapMode) {
                    bDragingMap = true;//拽动起始
                    dragStartPointX = mLastX;
                    dragStartPointY = mLastY;
                    pasteWidth = fSkiaBitmap.getWidth();
                    pasteHeight = fSkiaBitmap.getHeight();
                    pinchScaleFactor = 1;
                    scrnCenterPointGeo = mYimaLib.getGeoPoFromScrnPo(fSkiaBitmap.getWidth() / 2, fSkiaBitmap.getHeight() / 2);
                }
                //ShowShipInfo(mLastX, mLastY, 200000);
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.i(tag, "move");
                if (onMapClickListener != null) {
                    onMapClickListener.onMapTouched(MotionEvent.ACTION_MOVE);
                }
                dragingMap = true;

                mLastX = (int) event.getX();
                mLastY = (int) event.getY();
                int pointCount = event.getPointerCount();

                if (pointCount >= 2)//pinch-->随手指放大缩小
                {
                    Log.i("Pinch", "into");
                    if (bNormalDragMapMode) {
                        bDragingMap = false;
                        dragMapOffsetPointX = dragMapOffsetPointY = 0;
                    }
                    int painterIndex0 = event.findPointerIndex(0);
                    int painterIndex1 = event.findPointerIndex(1);
                    if (painterIndex0 == -1 || painterIndex1 == -1) {//三个手指缩放时，painterIndex1有时会获取到-1，导致崩溃
                        break;
                    }
                    Log.i("Pinch", "painterIndex0:" + String.valueOf(painterIndex0) + ",painterIndex1:" + String.valueOf(painterIndex1));
                    if ((mLastX0 == 0) || (mLastY0 == 0) || (mLastX1 == 0) || (mLastY1 == 0)) {
                        mLastX0 = (int) event.getX(painterIndex0);
                        mLastY0 = (int) event.getY(painterIndex0);
                        mLastX1 = (int) event.getX(painterIndex1);
                        mLastY1 = (int) event.getY(painterIndex1);
                        invalidate();
                        break;
                    }
                    mCurrX0 = (int) event.getX(painterIndex0);
                    mCurrY0 = (int) event.getY(painterIndex0);
                    mCurrX1 = (int) event.getX(painterIndex1);
                    mCurrY1 = (int) event.getY(painterIndex1);
                    double d1 = Math.sqrt(Math.pow(mLastX0 - mLastX1, 2) + Math.pow(mLastY0 - mLastY1, 2));
                    double d2 = Math.sqrt(Math.pow(mCurrX0 - mCurrX1, 2) + Math.pow(mCurrY0 - mCurrY1, 2));
                    double currentScaleFactor = d2 / d1;
                    if (currentScaleFactor == 1.0)
                        break;
//                    Log.i("Pinch", "currentScaleFactor:" + String.valueOf(currentScaleFactor) +
//                            "; Curr:(" + String.valueOf(mCurrX0) + "," + String.valueOf(mCurrY0)  +"),("+  String.valueOf(mCurrX1) + "," + String.valueOf(mCurrY1)  + ").");
//                    Point centerScrnPo = new Point(fSkiaBitmap.getWidth() / 2, fSkiaBitmap.getHeight() / 2);
//                    yimamapapi.skia.yimaclass.M_POINT centerGeoPo = new yimamapapi.skia.yimaclass.M_POINT();
//                    centerGeoPo = yimamapapi.skia.YimaLib.getGeoPoFromScrnPo(centerScrnPo.x, centerScrnPo.y);//由屏幕坐标获取地理坐标
//                    Point mouseScrnPo = new Point((mCurrX0 + mCurrX1) / 2, (mCurrY0 + mCurrY1) / 2);
//                    yimamapapi.skia.yimaclass.M_POINT  mouseGeoPo = new yimamapapi.skia.yimaclass.M_POINT();
//                    mouseGeoPo = yimamapapi.skia.YimaLib.getGeoPoFromScrnPo(mouseScrnPo.x, mouseScrnPo.y);
//                    Point newCenterGeoPo = new Point((int)(mouseGeoPo.x - (mouseGeoPo.x - centerGeoPo.x) / currentScaleFactor), (int)(mouseGeoPo.y - (mouseGeoPo.y - centerGeoPo.y) / currentScaleFactor));               i
                    if (bNormalDragMapMode) {
                        pasteWidth = (int) (pasteWidth * currentScaleFactor);
                        pasteHeight = (int) (pasteHeight * currentScaleFactor);
                        pinchScaleFactor = pinchScaleFactor * currentScaleFactor;
                        int dstOffsetX = (fSkiaBitmap.getWidth() - pasteWidth) / 2;
                        int dstOffsetY = (fSkiaBitmap.getHeight() - pasteHeight) / 2;
                        Log.i("Pinch", "pasteWidth:" + String.valueOf(pasteWidth) + ",pasteHeight:" + String.valueOf(pasteHeight) + ",dstOffsetX:" + String.valueOf(dstOffsetX) + ",dstOffsetY:" + String.valueOf(dstOffsetY));
                        mYimaLib.DrawScaledMap(dstOffsetX, dstOffsetY, pasteWidth, pasteHeight);
                    } else {
                        mYimaLib.SetCurrentScale(mYimaLib.GetCurrentScale() / (float) currentScaleFactor);//设置比例尺
                    }
                    mLastX0 = (int) event.getX(painterIndex0);
                    mLastY0 = (int) event.getY(painterIndex0);
                    mLastX1 = (int) event.getX(painterIndex1);
                    mLastY1 = (int) event.getY(painterIndex1);
                    invalidate();
                    break;
                }

                // 判断是否为拖动 电子屏不好点击，所以设成30，正常应该是0
                if (mLastY - mFirstY <= 30 && mLastX - mFirstX <= 30) {
                    dragingMap = false;
                }

                int iDragX = mLastX - mCurrX;
                int iDragY = mLastY - mCurrY;
                if ((iDragX == 0) && (iDragY == 0)) {
                    break;
                }
                if (bNormalDragMapMode && bDragingMap) {
                    dragMapOffsetPointX = iDragX;//mLastX - dragStartPointX;
                    dragMapOffsetPointY = iDragY;//mLastY - dragStartPointY;//curMouseScrnPo - dragStartPoint;
                    mYimaLib.PasteToScrn(dragMapOffsetPointX, dragMapOffsetPointY);
                } else mYimaLib.SetMapMoreOffset(iDragX, iDragY);//移动设置偏移

                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (onMapClickListener != null) {
                    onMapClickListener.onMapTouched(MotionEvent.ACTION_UP);
                }
//                Log.i(tag, "up");
                mLastX0 = mLastY0 = mLastX1 = mLastY1 = mFirstX = mFirstY = 0;

                // 不是拖动模式
                if (!dragingMap) {
                    if (onMapClickListener != null) {
                        onMapClickListener.onMapClicked(mYimaLib.getGeoPoFromScrnPo(mCurrX, mCurrY));
                    }
                }
                dragingMap = false;

                if (bNormalDragMapMode) {//留白模式
                    if (bDragingMap)//留白拖动结束
                        mYimaLib.SetMapMoreOffset((int) event.getX() - dragStartPointX, (int) event.getY() - dragStartPointY);
                    else {//pinch拖动结束
                        mYimaLib.CenterMap(scrnCenterPointGeo.x, scrnCenterPointGeo.y);
                        mYimaLib.SetCurrentScale(mYimaLib.GetCurrentScale() / (float) pinchScaleFactor);
                    }
                    bDragingMap = false;//拽动结束
                }
                invalidate();

                break;
            default:
                break;
        }
        return true;
    }

    //显示本船信息
    public void ShowShipInfo(int scrnX, int scrnY, int scale) {
        if (mYimaLib.GetCurrentScale() > scale) return;
        int retOtherVesselId = mYimaLib.SelectOtherVesselByScrnPoint(scrnX, scrnY);
        if (retOtherVesselId != -1) {
            int shipPos = mYimaLib.GetOtherVesselPosOfID(retOtherVesselId);
            OtherShipBaicInfo otherShipBaicInfo = mYimaLib.getOtherVesselBasicInfo(shipPos);
            OtherVesselCurrentInfo otherVesselCurrentInfo = mYimaLib.getOtherVesselCurrentInfo(shipPos);
            String strInfo = new String();
            strInfo = "船名:" + otherShipBaicInfo.strShipName + "\n" + "MMSI:" + otherShipBaicInfo.itrMmsi + "\n"
                    + "经度：" + String.valueOf(otherVesselCurrentInfo.currentPoint.x) + "\n" + "纬度：" + String.valueOf(otherVesselCurrentInfo.currentPoint.y) + "\n"
                    + "航速：" + otherVesselCurrentInfo.fSpeedOverGround + "\n" + "航向：" + otherVesselCurrentInfo.fCourseOverGround + "\n";
            new AlertDialog.Builder(mContext)
                    .setTitle("船舶")
                    .setMessage(strInfo)
                    .show();
        }
    }

    public OnMapClickListener onMapClickListener;

    public void setOnMapClickListener(OnMapClickListener onMapClickListener) {
        this.onMapClickListener = onMapClickListener;
    }

    public interface OnMapClickListener {
        void onMapClicked(M_POINT m_point);

        void onMapTouched(int action);
    }

    public void changeFishState(boolean show) {
        mYimaLib.SetIfShowFishArea(show);
        postInvalidate();
    }

    public boolean getFishState() {
        return mYimaLib.GetIfShowFishArea();
    }


    private int curLayerPos2 = -1;
    private int curLayerPos = -1;
    private final List<Integer> objLayerPos = new LinkedList<>();
    private final List<Integer> objLayerPos2 = new LinkedList<>();

    //添加线图层和物标
    public void AddLineLayerAndObject(List<LocationBean> list) {
        if (list == null || list.isEmpty()) return;
        clearTrack();
        mYimaLib.tmAppendLayer(2);//添加线图层
        curLayerPos = mYimaLib.tmGetLayerCount() - 1;
        mYimaLib.tmSetLayerName(curLayerPos, "线图层");
        mYimaLib.tmSetLayerDrawOrNot(curLayerPos, true);
        mYimaLib.tmAddLayerAttribute(curLayerPos, 4, "line");//添加图层属性：属性名称："物标名称",属性值类型：4（字符串）

        mYimaLib.tmAppendObjectInLayer(curLayerPos, 2);//在图层上添加一个线物标
        int objCount = mYimaLib.tmGetLayerObjectCount(curLayerPos);
        objLayerPos.add(objCount);
        int layerAttrCount = mYimaLib.tmGetLayerObjectAttrCount(curLayerPos);
        mYimaLib.tmSetObjectAttrValueString(curLayerPos, objCount - 1, layerAttrCount - 1
                , "line objaaaaaaaa");//设置物标属性值字符串
        int[] arrGeoX = new int[list.size()];
        int[] arrGeoY = new int[list.size()];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        for (int i = 0; i < list.size(); i++) {
            LocationBean lb = list.get(i);
            arrGeoX[i] = lb.getLongitude();
            arrGeoY[i] = lb.getLatitude();
        }
        mYimaLib.tmSetLineObjectCoors(curLayerPos, objCount - 1, list.size(), arrGeoX, arrGeoY);//设置物标坐标
        mYimaLib.tmSetLineObjectStyle(curLayerPos, objCount - 1, true, false,
                0, 0, 1, 10, 10, 255 - 10
                , 1, 0,
                "", "", 20
                , 0, 0, 0, false, false, 0, 0, false);//设置物标样式

        // 添加点
        mYimaLib.tmAppendLayer(1);//添加点图层
        curLayerPos2 = mYimaLib.tmGetLayerCount() - 1;
        mYimaLib.tmSetLayerName(curLayerPos2, "点图层");
        mYimaLib.tmSetLayerDrawOrNot(curLayerPos2, true);
        mYimaLib.tmAddLayerAttribute(curLayerPos2, 4, "point");//添加图层属性：属性名称："物标名称",属性值类型：4（字符串）
        for (int iObj = 0; iObj < arrGeoX.length; iObj++) {
            mYimaLib.tmAppendObjectInLayer(curLayerPos2, 0);//在图层上添加一个点物标
            objCount = mYimaLib.tmGetLayerObjectCount(curLayerPos2);
            objLayerPos2.add(objCount);
            //layerAttrCount = mYimaLib.tmGetLayerObjectAttrCount(curLayerPos2);
            //mYimaLib.tmSetObjectAttrValueString(curLayerPos2, objCount - 1, layerAttrCount - 1, sdf.format(list.get(iObj).getAcqtime()));//设置物标属性值字符串
            mYimaLib.tmSetPointObjectCoor(curLayerPos2, objCount - 1, arrGeoX[iObj], arrGeoY[iObj]);//设置物标坐标
            if (iObj == 0) {
                mYimaLib.tmSetPointObjectStyle(curLayerPos2, objCount - 1, 1,
                        true, iObj * 10, iObj * 10, 255 - iObj * 10, 20,
                        sdf.format(list.get(iObj).getAcqtime()), "", 20, 0, 0, 0, true, false, 0, 0, 0, 0);//设置物标样式
            } else if (iObj == arrGeoX.length - 1) {
                mYimaLib.tmSetPointObjectStyle(curLayerPos2, objCount - 1, 3,
                        true, iObj * 10, iObj * 10, 255 - iObj * 10, 20,
                        sdf.format(list.get(iObj).getAcqtime()), "", 20, 0, 0, 0, true, false, 0, 0, 0, 0);//设置物标样式
            } else if (iObj > 0) {
                mYimaLib.tmSetPointObjectStyle(curLayerPos2, objCount - 1, 2,
                        true, iObj * 10, iObj * 10, 255 - iObj * 10, 20,
                        sdf.format(list.get(iObj).getAcqtime()), "", 20, 0, 0, 0, true, false, 0, 0, 0, 0);//设置物标样式
            }
        }
        postInvalidate();
        Toast.makeText(mContext, "轨迹显示完成", Toast.LENGTH_SHORT).show();
    }

    public void clearTrack() {
        if (curLayerPos2 != -1) {
            for (int a : objLayerPos2) {
                mYimaLib.tmDeleteGeoObject(curLayerPos2, a);
            }
            objLayerPos2.clear();
            mYimaLib.tmDeleteLayer(curLayerPos2);
            curLayerPos2 = -1;
        }
        if (curLayerPos != -1) {
            for (int a : objLayerPos) {
                mYimaLib.tmDeleteGeoObject(curLayerPos, a);
            }
            objLayerPos.clear();
            mYimaLib.tmDeleteLayer(curLayerPos);
            curLayerPos = -1;
        }
        postInvalidate();
    }


    /**
     * 绘制 禁渔区 禁入区 禁出区
     */
    public int drawBanArea(List<WarnArea> areas) {
        mYimaLib.tmAppendLayer(3);//添加面图层
        int curLayerPos = mYimaLib.tmGetLayerCount() - 1;
        mYimaLib.tmSetLayerName(curLayerPos, "面图层图层");
        mYimaLib.tmSetLayerDrawOrNot(curLayerPos, true);
        mYimaLib.tmAddLayerAttribute(curLayerPos, 4, "物标名称");//添加图层属性：属性名称："物标名称",属性值类型：4（字符串）
        for (int i = 0; i < areas.size(); i++) {
            WarnArea area = areas.get(i);
            mYimaLib.tmAppendObjectInLayer(curLayerPos, 3);//在图层上添加一个面物标
            int objCount = mYimaLib.tmGetLayerObjectCount(curLayerPos);
            area.setCurLayerPos(curLayerPos);
            area.setObjCount(objCount);
            String typeStr = "";
            switch (area.getType()) {
                case 0:
                    typeStr = "禁渔区";
                    break;
                case 1:
                    typeStr = "禁入区";
                    break;
                case 2:
                    typeStr = "禁出区";
                    break;
            }

            //int layerAttrCount = mYimaLib.tmGetLayerObjectAttrCount(curLayerPos);
            //mYimaLib.tmSetObjectAttrValueString(curLayerPos, objCount - 1, layerAttrCount - 1, "face obj");//设置物标属性值字符串
            List<Integer> listX = area.getGeoX();
            List<Integer> listY = area.getGeoY();
            int[] geoX = new int[listX.size()];
            int[] geoY = new int[listY.size()];
            for (int j = 0; j < listX.size(); j++) {
                geoX[j] = listX.get(j);
                geoY[j] = listY.get(j);
            }
            mYimaLib.tmSetFaceObjectCoors(curLayerPos, objCount - 1, geoX.length, geoX, geoY);//设置物标坐标
            try {
                mYimaLib.tmSetFaceObjectStyle(curLayerPos, objCount - 1, true, area.getRed(), area.getGreen(), area.getBlue()
                        , 50, 1, 0, 0
                        , new String(typeStr.getBytes("GBK"), "GBK"), "宋体", 20, 0, 0
                        , 0, true, false, true, 0, 0);//设置物标样式
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        postInvalidate();
        return curLayerPos;
    }

    /**
     * 移除 禁渔区 禁入区 禁出区
     *
     * @param list
     * @return
     */
    public boolean removeBanArea(List<Integer> list) {
        try {
            if (list == null || list.isEmpty()) return true;
            if (list.size() < 2) return false;
            mYimaLib.tmDeleteGeoObject(list.get(0), list.get(1));
            mYimaLib.tmDeleteLayer(list.get(0));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}

