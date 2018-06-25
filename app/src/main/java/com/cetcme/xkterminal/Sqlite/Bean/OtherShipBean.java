package com.cetcme.xkterminal.Sqlite.Bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.sql.Timestamp;
import java.util.Date;

@Table(name="t_other_ship")
public class OtherShipBean {
    @Column(name = "id", isId = true)
    private int id;
    @Column(name="mmsi")
    private int mmsi;
    @Column(name="ship_id")
    private int ship_id;
    @Column(name="ship_name")
    private String ship_name;
    @Column(name="acq_time")
    private Date acq_time;

    private boolean show = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMmsi() {
        return mmsi;
    }

    public void setMmsi(int mmsi) {
        this.mmsi = mmsi;
    }

    public int getShip_id() {
        return ship_id;
    }

    public void setShip_id(int ship_id) {
        this.ship_id = ship_id;
    }

    public String getShip_name() {
        return ship_name;
    }

    public void setShip_name(String ship_name) {
        this.ship_name = ship_name;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public Date getAcq_time() {
        return acq_time;
    }

    public void setAcq_time(Date acq_time) {
        this.acq_time = acq_time;
    }

    @Override
    public String toString() {
        return "OtherShipBean{" +
                "id=" + id +
                ", mmsi=" + mmsi +
                ", ship_id=" + ship_id +
                ", ship_name='" + ship_name + '\'' +
                ", acq_time=" + acq_time +
                ", show=" + show +
                '}';
    }
}
