package com.changgou.order.pojo;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "tb_task")
public class Task {

    @Id
    private Integer id;   //任务id

    private Date createTime;

    private Date updateTime;

    private Date deleteTime;

    private String taskType;   //任务类型

    private String mqExchange; //交换机名称

    private String mqRoutingkey;   //routing key

    private String requestBody;    //任务请求内容

    private String status;          //任务请求

    private String errormsg;       //任务错误信息

    public Task() {
    }

    public Task(Integer id, Date createTime, Date updateTime, Date deleteTime, String taskType, String mqExchange, String mqRoutingKey, String requestBody, String status, String errorMsg) {
        this.id = id;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.deleteTime = deleteTime;
        this.taskType = taskType;
        this.mqExchange = mqExchange;
        this.mqRoutingkey = mqRoutingKey;
        this.requestBody = requestBody;
        this.status = status;
        this.errormsg = errorMsg;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Date deleteTime) {
        this.deleteTime = deleteTime;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getMqExchange() {
        return mqExchange;
    }

    public void setMqExchange(String mqExchange) {
        this.mqExchange = mqExchange;
    }

    public String getMqRoutingkey() {
        return mqRoutingkey;
    }

    public void setMqRoutingkey(String mqRoutingkey) {
        this.mqRoutingkey = mqRoutingkey;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errormsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errormsg = errorMsg;
    }
}
