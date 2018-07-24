package com.cetcme.xkterminal.util;

import android.graphics.Bitmap;
import android.view.View;

import java.util.List;

import yimamapapi.skia.YimaLib;

public class YimUtil {

    private static final YimaLib yimaLib = new YimaLib();
    private static boolean isInit = false;
    private static int pointLayer = -1;
    private static int lineLayer = -1;
    private static int faceLayer = -1;

    private YimUtil() {
    }

    public static YimaLib initYima(String workPath) {
        if (!isInit) {
            yimaLib.Create();
            yimaLib.Init(workPath);
            isInit = true;
            pointLayer = addLayer(1);
            lineLayer = addLayer(2);
            faceLayer = addLayer(3);
            yimaLib.tmSetLayerName(pointLayer, "point");
            yimaLib.tmSetLayerName(lineLayer, "line");
            yimaLib.tmSetLayerName(faceLayer, "face");
            hideLayers();
        }
        return yimaLib;
    }

    public static YimaLib lib() throws Exception {
        if (!isInit) {
            throw new Exception("must call method initYima to init");
        }
        return yimaLib;
    }

    public static Bitmap refresh(String workPath, int w, int h, int mode) throws Exception {
        if (!isInit) {
            throw new Exception("must call method initYima to init");
        }
        Bitmap fSkiaBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        String strTtfFilePath = workPath + "/DroidSansFallback.ttf";
        yimaLib.SetDisplayCategory(mode);
        yimaLib.RefreshDrawer(fSkiaBitmap, strTtfFilePath);//刷新绘制器，需要传入字体文件地址，用户可以自己修改为别的字体
        yimaLib.OverViewLibMap(0);//概览第一幅图
        return fSkiaBitmap;
    }

    private static void hideLayers() {
        yimaLib.tmSetLayerDrawOrNot(pointLayer, false);
        yimaLib.tmSetLayerDrawOrNot(lineLayer, false);
        yimaLib.tmSetLayerDrawOrNot(faceLayer, false);
    }

    /**
     * @param view   地图载体
     * @param isShow 是否显示 true显示 false不显示
     * @throws Exception
     */
    public static void showFishArea(View view, boolean isShow) throws Exception {
        if (!isInit) {
            throw new Exception("must call method initYima to init");
        }
        if (view == null) {
            throw new Exception("view can not be null");
        }
        yimaLib.SetIfShowFishArea(isShow);
        view.postInvalidate();
    }

    /**
     * 添加多个点
     *
     * @param view
     * @param symbolId
     * @param geoX
     * @param geoY
     * @param pointsCount
     * @throws Exception
     */
    public static void addPoints(View view, int symbolId, int[] geoX, int[] geoY, int pointsCount) throws Exception {
        if (!isInit) {
            throw new Exception("must call method initYima to init");
        }
        if (view == null) {
            throw new Exception("view can not be null");
        }
        if (pointsCount < 1) {
            throw new Exception("please input some point information");
        }
        int layerPos = pointLayer;
        hideLayers();
        removeAllILayer(layerPos);
        for (int i = 0; i < pointsCount; i++) {
            int iLayerPos = addILayer(layerPos, 0);
            yimaLib.tmSetPointObjectCoor(layerPos, iLayerPos, geoX[i], geoY[i]);// 设置点物标经纬度
            yimaLib.tmSetPointObjectStyle(layerPos, iLayerPos, symbolId, false,
                    0, 0, 0,
                    12, "宋体", null, 12, 0, 0,
                    0, false, false, 0, 0, 0, 0);
        }
        yimaLib.tmSetLayerDrawOrNot(layerPos, true);
        view.postInvalidate();
    }

    /**
     * 添加多个线物标
     *
     * @param view
     * @param geoXList
     * @param geoYList
     * @param pointsCountList
     * @throws Exception
     */
    public static void addLines(View view, List<Integer[]> geoXList, List<Integer[]> geoYList, List<Integer> pointsCountList) throws Exception {
        if (!isInit) {
            throw new Exception("must call method initYima to init");
        }
        if (view == null) {
            throw new Exception("view can not be null");
        }

        int size = pointsCountList == null ? 0 : pointsCountList.size();
        if (size < 0) {
            throw new Exception("please input some line information");
        }

        int layerPos = lineLayer;
        hideLayers();
        removeAllILayer(layerPos);
        for (int i = 0; i < size; i++) {
            Integer[] geoXT = geoXList.get(i);
            int[] geoX = integer2Int(geoXT);
            Integer[] geoYT = geoYList.get(i);
            int[] geoY = integer2Int(geoYT);
            Integer pointsCount = pointsCountList.get(i);

            int iLayerPos = addILayer(layerPos, 2);
            yimaLib.tmSetLineObjectCoors(layerPos, iLayerPos, pointsCount, geoX, geoY);
        }
        yimaLib.tmSetLayerDrawOrNot(layerPos, true);
        view.postInvalidate();
    }

    /**
     * 添加多个面物标
     *
     * @param view
     * @param geoXList
     * @param geoYList
     * @param pointsCountList
     * @throws Exception
     */
    public static void addFaces(View view, List<Integer[]> geoXList, List<Integer[]> geoYList, List<Integer> pointsCountList) throws Exception {
        if (!isInit) {
            throw new Exception("must call method initYima to init");
        }
        if (view == null) {
            throw new Exception("view can not be null");
        }
        int size = pointsCountList == null ? 0 : pointsCountList.size();
        if (size < 0) {
            throw new Exception("please input some face information");
        }
        int layerPos = faceLayer;
        hideLayers();
        removeAllILayer(layerPos);
        for (int i = 0; i < size; i++) {
            Integer[] geoXT = geoXList.get(i);
            int[] geoX = integer2Int(geoXT);
            Integer[] geoYT = geoYList.get(i);
            int[] geoY = integer2Int(geoYT);
            Integer pointsCount = pointsCountList.get(i);
            int iLayerPos = addILayer(layerPos, 3);
            yimaLib.tmSetFaceObjectCoors(layerPos, iLayerPos, pointsCount, geoX, geoY);
        }
        yimaLib.tmSetLayerDrawOrNot(layerPos, true);
        view.postInvalidate();
    }

    private static void removeAllILayer(int layerPos) {
        int count = yimaLib.tmGetLayerObjectCount(layerPos);
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                removeILayer(layerPos, i);
            }
        }
    }

    public static void clearAll() {
        if (pointLayer != -1) {
            removeAllILayer(pointLayer);
        }
        if (lineLayer != -1) {
            removeAllILayer(lineLayer);
        }
        if (faceLayer != -1) {
            removeAllILayer(faceLayer);
        }
        hideLayers();
    }

    /**
     * 添加图层
     *
     * @param type LAYER_GEO_TYPE_NULL(0)
     *             ALL_POINT(1)
     *             ALL_LINE(2)
     *             ALL_FACE(3)
     *             ALL_STRING(6)
     *             MULTIPLE_GEO_TYPE(5)
     * @return 返回新添加图层的索引
     */
    private static int addLayer(int type) {
        yimaLib.tmAppendLayer(type);
        return yimaLib.tmGetLayerCount() - 1;
    }

    /**
     * 删除图层
     *
     * @param layerPos 图层索引
     * @return 删除图层是否成功
     */
    private static boolean removeLayer(int layerPos) {
        return yimaLib.tmDeleteLayer(layerPos);
    }

    /**
     * 添加物标
     *
     * @param layerPos 图层索引
     * @param type     TYPE_NULL(-1)
     *                 TYPE_POINT (0)
     *                 TYPE_LINE (2)
     *                 TYPE_FACE(3)
     *                 TYPE_COMBINED_OBJECT(10)
     * @return 物标索引
     */
    private static int addILayer(int layerPos, int type) {
        boolean res = yimaLib.tmAppendObjectInLayer(layerPos, type);
        if (res) {
            return yimaLib.tmGetLayerObjectCount(layerPos) - 1;
        } else {
            return -1;
        }
    }

    /**
     * 删除物标
     *
     * @param layerPos  图层索引
     * @param iLayerPos 物标索引
     * @return 删除物标是否成功
     */
    private static boolean removeILayer(int layerPos, int iLayerPos) {
        return yimaLib.tmDeleteGeoObject(layerPos, iLayerPos);
    }

    private static int[] integer2Int(Integer[] geoT) {
        if (geoT == null) return new int[0];
        int[] arr = new int[geoT.length];
        for (int i = 0; i < geoT.length; i++) {
            arr[i] = geoT[i];
        }
        return arr;
    }

}
