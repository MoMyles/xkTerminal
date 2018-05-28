package com.cetcme.xkterminal.Sqlite.Bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name="t_group")
public class GroupBean {
    @Column(name = "id", isId = true)
    private int id;
    @Column(name = "number")
    private int number;
    @Column(name = "name")
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "GroupBean{" +
                "id=" + id +
                ", number=" + number +
                ", name='" + name + '\'' +
                '}';
    }
}
