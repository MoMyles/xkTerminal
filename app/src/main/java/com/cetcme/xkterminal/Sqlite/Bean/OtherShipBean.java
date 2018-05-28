package com.cetcme.xkterminal.Sqlite.Bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name="t_other_ship")
public class OtherShipBean {
    @Column(name = "id", isId = true)
    private int id;
    @Column(name="mmsi")
    private int mmsi;
    @Column(name="ship_id")
    private int ship_id;

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

    @Override
    public String toString() {
        return "OtherShipBean{" +
                "id=" + id +
                ", mmsi=" + mmsi +
                ", ship_id=" + ship_id +
                '}';
    }
}
