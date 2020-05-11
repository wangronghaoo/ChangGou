package com.changgou.user.pojo;

import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tb_point_log")
public class PointLog {

    private String orderId;

    private String userId;

    private Integer point;


    public PointLog(String orderId, String userId, Integer point) {
        this.orderId = orderId;
        this.userId = userId;
        this.point = point;
    }

    public PointLog() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getPoint() {
        return point;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }
}
