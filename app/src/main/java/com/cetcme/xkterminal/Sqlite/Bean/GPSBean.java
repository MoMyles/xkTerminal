package com.cetcme.xkterminal.Sqlite.Bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.util.Date;

/**
 * Created by dell on 2018/6/1.
 */
@Table(name="t_gps_weixing")
public class GPSBean {
    @Column(name = "id", isId = true)
    private int id;
    @Column(name = "no")
    private int no;
    @Column(name = "yangjiao")
    private int yangjiao;
    @Column(name = "fangwei")
    private int fangwei;
    @Column(name = "xinhao")
    private int xinhao;
    @Column(name="created")
    private long created;


    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public int getXinhao() {
        return xinhao;
    }

    public void setXinhao(int xinhao) {
        this.xinhao = xinhao;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public int getYangjiao() {
        return yangjiao;
    }

    public void setYangjiao(int yangjiao) {
        this.yangjiao = yangjiao;
    }

    public int getFangwei() {
        return fangwei;
    }

    public void setFangwei(int fangwei) {
        this.fangwei = fangwei;
    }
}
