package com.cetcme.xkterminal.Sqlite.Bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by qiuhong on 03/04/2018.
 */
@Table(name = "t_inout")
public class InoutBean implements Serializable{

    @Column(name = "id", isId = true)
    private int id;
    @Column(name = "type")
    private int type; // 1出港 2进港
    @Column(name = "count")
    private int count; // 人数
    @Column(name = "lon")
    private int lon; // 经度
    @Column(name = "lat")
    private int lat; // 纬度
    @Column(name = "time")
    private Date time;

    @Override
    public String toString() {
        return "InoutBean{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", count='" + count + '\'' +
                ", lon='" + lon + '\'' +
                ", lat='" + lat + '\'' +
                ", time='" + time + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getLon() {
        return lon;
    }

    public void setLon(int lon) {
        this.lon = lon;
    }

    public int getLat() {
        return lat;
    }

    public void setLat(int lat) {
        this.lat = lat;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
