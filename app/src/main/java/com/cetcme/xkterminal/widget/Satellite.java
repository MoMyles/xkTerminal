package com.cetcme.xkterminal.widget;

/**
 * Created by dell on 2018/5/24.
 */

public class Satellite {
    private int num;// 信号强度
    private String elevationAngle;// 仰角 0 - 90
    private String azimuth;// 方位角 0 - 359
    private int snr = 0;
    private String satelliteType;// 卫星类型

    public Satellite() {
    }

    public int getNum() {
        return this.num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getElevationAngle() {
        return this.elevationAngle;
    }

    public void setElevationAngle(String elevationAngle) {
        this.elevationAngle = elevationAngle;
    }

    public String getAzimuth() {
        return this.azimuth;
    }

    public void setAzimuth(String azimuth) {
        this.azimuth = azimuth;
    }

    public int getSnr() {
        return this.snr;
    }

    public void setSnr(int snr) {
        this.snr = snr;
    }

    public String getSatelliteType() {
        return this.satelliteType;
    }

    public void setSatelliteType(String satelliteType) {
        this.satelliteType = satelliteType;
    }

}
