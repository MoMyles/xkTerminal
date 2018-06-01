package com.cetcme.xkterminal.Navigation;

import java.util.Arrays;
import java.util.List;

public class WarnArea {
    private int red;
    private int green;
    private int blue;
    private int curLayerPos;
    private int objCount;
    private int type = 0;//禁渔 0 禁入 1 禁出 2

    private List<Integer> geoX;
    private List<Integer> geoY;

    public WarnArea(int red, int green, int blue, List<Integer> geoX, List<Integer> geoY) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.geoX = geoX;
        this.geoY = geoY;
    }
    public WarnArea(int type, int red, int green, int blue, Integer[] geoX, Integer[] geoY) {
        this.type = type;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.geoX = Arrays.asList(geoX);
        this.geoY = Arrays.asList(geoY);
    }

    public int getType() {
        return type;
    }

    public int getCurLayerPos() {
        return curLayerPos;
    }

    public void setCurLayerPos(int curLayerPos) {
        this.curLayerPos = curLayerPos;
    }

    public int getObjCount() {
        return objCount;
    }

    public void setObjCount(int objCount) {
        this.objCount = objCount;
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public List<Integer> getGeoX() {
        return geoX;
    }

    public void setGeoX(List<Integer> geoX) {
        this.geoX = geoX;
    }

    public List<Integer> getGeoY() {
        return geoY;
    }

    public void setGeoY(List<Integer> geoY) {
        this.geoY = geoY;
    }
}
