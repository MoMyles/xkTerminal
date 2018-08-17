package com.cetcme.xkterminal.Navigation;

public class MapUtils {
    public static boolean isPtInPoly(int ALon, int ALat, int[] geoX, int[] geoY) {
        int iSum, iCount, iIndex;
        double dLon1 = 0, dLon2 = 0, dLat1 = 0, dLat2 = 0, dLon;
        if (geoX.length < 3) {
            return false;
        }
        iSum = 0;
        iCount = geoX.length;
        for (iIndex = 0; iIndex < iCount; iIndex++) {
            if (iIndex == iCount - 1) {
                dLon1 = geoX[iIndex];
                dLat1 = geoY[iIndex];
                dLon2 = geoX[0];
                dLat2 = geoY[0];
            } else {
                dLon1 = geoX[iIndex];
                dLat1 = geoY[iIndex];
                dLon2 = geoX[iIndex + 1];
                dLat2 = geoY[iIndex + 1];
            }
            // 以下语句判断A点是否在边的两端点的水平平行线之间，在则可能有交点，开始判断交点是否在左射线上
            if (((ALat >= dLat1) && (ALat < dLat2)) || ((ALat >= dLat2) && (ALat < dLat1))) {
                if (Math.abs(dLat1 - dLat2) > 0) {
                    //得到 A点向左射线与边的交点的x坐标：
                    dLon = dLon1 - ((dLon1 - dLon2) * (dLat1 - ALat)) / (dLat1 - dLat2);
                    // 如果交点在A点左侧（说明是做射线与 边的交点），则射线与边的全部交点数加一：
                    if (dLon < ALon) {
                        iSum++;
                    }
                }
            }
        }
        if ((iSum % 2) != 0) {
            return true;
        }
        return false;
    }
}
