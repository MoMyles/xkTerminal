package com.cetcme.xkterminal.widget;

/**
 * Created by dell on 2018/5/24.
 */

public class Satellite {
    private int no;
    private int num;// 信号强度
    private int elevationAngle;// 仰角 0 - 90
    private int azimuth;// 方位角 0 - 359
    private int snr = 0;
    private String satelliteType;// 卫星类型

    public Satellite() {
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public int getNum() {
        return this.num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getElevationAngle() {
        return elevationAngle;
    }

    public void setElevationAngle(int elevationAngle) {
        this.elevationAngle = elevationAngle;
    }

    public int getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(int azimuth) {
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
