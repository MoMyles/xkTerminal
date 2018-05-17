package com.cetcme.xkterminal.Navigation;

import java.sql.Time;
import java.util.Calendar;



/**
 * Created by promoter on 2017/9/1.
 */

public class GpsParse {

    public static GpsInfo parse(String gpsStr){
        if(gpsStr.length() < 10)    return null;
        GpsInfo gpsInfo = new GpsInfo();
        String strHead = gpsStr.substring(0, 6);
        switch (strHead) {
            case "$GPGGA":{
                String[] arrStr = gpsStr.split(",");
                if(arrStr.length < 9 ) return null;
                String timeStr = arrStr[1];
                if(timeStr.length() >= 6) {
                    String timeHour = timeStr.substring(0, 2);
                    String timeMinute = timeStr.substring(2, 4);
                    String timeSecond = timeStr.substring(4, 6);
                    gpsInfo.cal1 = Calendar.getInstance();
                    gpsInfo.cal1.set(Calendar.HOUR, Integer.valueOf(timeHour));
                    gpsInfo.cal1.set(Calendar.MINUTE, Integer.valueOf(timeMinute));
                    gpsInfo.cal1.set(Calendar.SECOND, Integer.valueOf(timeSecond));
                }

                String longStr = arrStr[2];
                String longStr2 = arrStr[3];
                if(longStr2.equals("N"))   {
                    if(!longStr.equals(""))
                        gpsInfo.longtitude =  (int)(Float.valueOf(longStr) * 100000);
                }
                else  {
                    if(!longStr.equals(""))
                        gpsInfo.longtitude =  (int)(- Float.valueOf(longStr) * 100000);
                }

                String latiStr = arrStr[4];
                String latiStr2 = arrStr[5];
                if(latiStr2.equals("E"))   {
                    if(!latiStr.equals(""))
                        gpsInfo.latititude =  (int)(Float.valueOf(latiStr) * 100000);
                }
                else   {
                    if(!latiStr.equals(""))
                        gpsInfo.latititude =  (int)(- Float.valueOf(latiStr) * 100000);
                }
                break;
            }
            case "$GPGLL":{
                String[] arrStr = gpsStr.split(",");
                if(arrStr.length < 6 ) return null;
                String timeStr = arrStr[5];
                if(timeStr.length() >= 6) {
                    String timeHour = timeStr.substring(0, 2);
                    String timeMinute = timeStr.substring(2, 4);
                    String timeSecond = timeStr.substring(4, 6);
                    gpsInfo.cal1 = Calendar.getInstance();
                    gpsInfo.cal1.set(Calendar.HOUR, Integer.valueOf(timeHour));
                    gpsInfo.cal1.set(Calendar.MINUTE, Integer.valueOf(timeMinute));
                    gpsInfo.cal1.set(Calendar.SECOND, Integer.valueOf(timeSecond));
                }
                String longStr = arrStr[1];
                String longStr2 = arrStr[2];
                if(longStr2.equals("N"))   {
                    if(!longStr.equals(""))
                        gpsInfo.longtitude =  (int)(Float.valueOf(longStr) * 100000);
                }
                else   {
                    if(!longStr.equals(""))
                        gpsInfo.longtitude =  (int)(- Float.valueOf(longStr) * 100000);
                }

                String latiStr = arrStr[3];
                String latiStr2 = arrStr[4];
                if(latiStr2.equals("E"))   {
                    if(!latiStr.equals(""))
                        gpsInfo.latititude =  (int)(Float.valueOf(latiStr) * 100000);
                }
                else   {
                    if(!latiStr.equals(""))
                        gpsInfo.latititude =  (int)(- Float.valueOf(latiStr) * 100000);
                }
                break;
            }
            case "$GPRMC":{
                String[] arrStr = gpsStr.split(",");
                if(arrStr.length < 10 ) return null;
                String timeStr = arrStr[1];
                String dateStr = arrStr[9];
                if(timeStr.length() >= 6 && dateStr.length() >= 6) {
                    String dateDD = dateStr.substring(0, 2);
                    String dateMM = dateStr.substring(2, 4);
                    String dateYY = dateStr.substring(4, 6);
                    String timeHour = timeStr.substring(0, 2);
                    String timeMinute = timeStr.substring(2, 4);
                    String timeSecond = timeStr.substring(4, 6);
                    gpsInfo.cal1 = Calendar.getInstance();
                    gpsInfo.cal1.set(Calendar.DATE, Integer.valueOf(dateDD));
                    gpsInfo.cal1.set(Calendar.MONTH, Integer.valueOf(dateMM) - 1);
                    gpsInfo.cal1.set(Calendar.YEAR, 2000 + Integer.valueOf(dateYY));
                    gpsInfo.cal1.set(Calendar.HOUR, Integer.valueOf(timeHour));
                    gpsInfo.cal1.set(Calendar.MINUTE, Integer.valueOf(timeMinute));
                    gpsInfo.cal1.set(Calendar.SECOND, Integer.valueOf(timeSecond));
                }
                String longStr = arrStr[5];
                String longStr2 = arrStr[6];
                if(longStr2.equals("E"))   {
                    if(!longStr.equals(""))
                        gpsInfo.longtitude =  (int)(Float.valueOf(longStr) * 100000);
                }
                else   {
                    if(!longStr.equals(""))
                        gpsInfo.longtitude =  (int)(- Float.valueOf(longStr) * 100000);
                }

                String latiStr = arrStr[3];
                String latiStr2 = arrStr[4];
                if(latiStr2.equals("N"))   {
                    if(!latiStr.equals(""))
                        gpsInfo.latititude =  (int)(Float.valueOf(latiStr) * 100000);
                }
                else  {
                    if(!latiStr.equals(""))
                        gpsInfo.latititude =  (int)(- Float.valueOf(latiStr) * 100000);
                }

                String speedStr = arrStr[7];
                String courseStr = arrStr[8];
                if(!speedStr.equals(""))
                    gpsInfo.speed =  Float.valueOf(speedStr);
                if(!courseStr.equals(""))
                    gpsInfo.course =  Float.valueOf(courseStr);
                break;
            }
            default:    return null;
        }
        return gpsInfo;
    }
}

//根据GPS NMEA-0183解析
//    $GPGGA
//
//            例：$GPGGA,092204.999,4250.5589,S,14718.5084,E,1,04,24.4,19.7,M,,,,0000*1F
//
//            字段0：$GPGGA，语句ID，表明该语句为Global Positioning System Fix Data（GGA）GPS定位信息
//
//            字段1：UTC 时间，hhmmss.sss，时分秒格式
//
//            字段2：纬度ddmm.mmmm，度分格式（前导位数不足则补0）
//
//            字段3：纬度N（北纬）或S（南纬）
//
//            字段4：经度dddmm.mmmm，度分格式（前导位数不足则补0）
//
//            字段5：经度E（东经）或W（西经）
//
//            字段6：GPS状态，0=未定位，1=非差分定位，2=差分定位，3=无效PPS，6=正在估算
//
//            字段7：正在使用的卫星数量（00 - 12）（前导位数不足则补0）
//
//            字段8：HDOP水平精度因子（0.5 - 99.9）
//
//            字段9：海拔高度（-9999.9 - 99999.9）
//
//            字段10：地球椭球面相对大地水准面的高度
//
//            字段11：差分时间（从最近一次接收到差分信号开始的秒数，如果不是差分定位将为空）
//
//            字段12：差分站ID号0000 - 1023（前导位数不足则补0，如果不是差分定位将为空）
//
//            字段13：校验值

//    $GPGLL
//
//            例：$GPGLL,4250.5589,S,14718.5084,E,092204.999,A*2D
//
//            字段0：$GPGLL，语句ID，表明该语句为Geographic Position（GLL）地理定位信息
//
//            字段1：纬度ddmm.mmmm，度分格式（前导位数不足则补0）
//
//            字段2：纬度N（北纬）或S（南纬）
//
//            字段3：经度dddmm.mmmm，度分格式（前导位数不足则补0）
//
//            字段4：经度E（东经）或W（西经）
//
//            字段5：UTC时间，hhmmss.sss格式
//
//            字段6：状态，A=定位，V=未定位
//
//            字段7：校验值

//    $GPRMC
//
//            例：$GPRMC,024813.640,A,3158.4608,N,11848.3737,E,10.05,324.27,150706,,,A*50
//
//            字段0：$GPRMC，语句ID，表明该语句为Recommended Minimum Specific GPS/TRANSIT Data（RMC）推荐最小定位信息
//
//            字段1：UTC时间，hhmmss.sss格式
//
//            字段2：状态，A=定位，V=未定位
//
//            字段3：纬度ddmm.mmmm，度分格式（前导位数不足则补0）
//
//            字段4：纬度N（北纬）或S（南纬）
//
//            字段5：经度dddmm.mmmm，度分格式（前导位数不足则补0）
//
//            字段6：经度E（东经）或W（西经）
//
//            字段7：速度，节，Knots
//
//            字段8：方位角，度
//
//            字段9：UTC日期，DDMMYY格式
//
//            字段10：磁偏角，（000 - 180）度（前导位数不足则补0）
//
//            字段11：磁偏角方向，E=东W=西
//
//            字段16：校验值
