package com.cetcme.xkterminal.Sqlite.Bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name= "t_pin")
public class PinBean {

    @Column(name="id", isId = true)
    private int id;

    @Column(name="lon")
    private int lon;

    @Column(name="lat")
    private int lat;

    @Column(name="name")
    private String name;

    @Column(name="color")
    private int color;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "PinBean{" +
                "id=" + id +
                ", lon=" + lon +
                ", lat=" + lat +
                ", name='" + name + '\'' +
                ", color=" + color +
                '}';
    }
}
