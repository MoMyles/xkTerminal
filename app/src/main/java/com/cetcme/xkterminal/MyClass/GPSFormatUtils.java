package com.cetcme.xkterminal.MyClass;

import com.cetcme.xkterminal.DataFormat.Util.ConvertUtil;

import java.util.Date;

public class GPSFormatUtils {

    /**
     * 功能：         度-->度分秒
     *
     * @param d 传入待转化格式的经度或者纬度
     */
    public static String DDtoDMS(Double d, boolean isJd) {

        String[] array = d.toString().split("[.]");
        String degrees = array[0];//得到度
        if (d < 0) {
            // 西经 南纬
            degrees = degrees.replace("-", "");
            if (isJd) {
                degrees = "W " + degrees;
            } else {
                degrees = "S " + degrees;
            }
        } else {
            // 东经  北纬
            if (isJd) {
                degrees = "E " + degrees;
            } else {
                degrees = "N " + degrees;
            }
        }

        Double m = Double.parseDouble("0." + array[1]) * 60;
        String[] array1 = m.toString().split("[.]");
        int minutes = Integer.parseInt(array1[0]);//得到分

        Double s = Double.parseDouble("0." + array1[1]) * 60;
        String[] array2 = s.toString().split("[.]");
        int seconds = Integer.parseInt(array2[0]);//得到秒
        return degrees + "°" + String.format("%.2f", m) + "’";
    }

    /**
     * 功能：         度-->度分
     *
     * @param d 传入待转化格式的经度或者纬度
     */
    public static byte[] formatGps(Double d) {

        String[] array = d.toString().split("[.]");
        String degrees = array[0];//得到度
        if (degrees.length() == 1) degrees = "00" + degrees;
        if (degrees.length() == 2) degrees = "0" + degrees;

        Double m = Double.parseDouble("0." + array[1]) * 60;
        String[] array1 = m.toString().split("[.]");
        String minutes = array1[0];//得到分
        if (minutes.length() == 1) minutes = "0" + minutes;

        String s = array1[1];
        if (s.length() == 1) s = s + "00";
        if (s.length() == 2) s = s + "0";
        if (s.length() > 3) s = s.substring(0, 3);

        return ConvertUtil.str2Bcd(degrees + minutes + s);
    }

    public static void main(String[] args) {
//        System.out.println(ConvertUtil.bytesToHexString(formatGps((double) 1212376000 / 10000000)));
//        System.out.println(DDtoDMS(121.23)[2]);

        String dateStr = DateUtil.Date2String(new Date(), "yyMMddHHmmss");
        byte[] timeBytes = ConvertUtil.str2Bcd(dateStr);
        System.out.println(ConvertUtil.bytesToHexString(timeBytes));
    }
}