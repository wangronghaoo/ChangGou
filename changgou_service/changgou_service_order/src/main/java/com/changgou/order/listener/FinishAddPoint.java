package com.changgou.order.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.config.MqConfig;
import com.changgou.order.dao.TaskHisMapper;
import com.changgou.order.dao.TaskMapper;
import com.changgou.order.pojo.Task;
import com.changgou.order.pojo.TaskHis;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class FinishAddPoint {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskHisMapper taskHisMapper;


    @RabbitListener(queues = MqConfig.FinishAddPointQueue)
    public void delTask(String message) {

        Task task = JSON.parseObject(message, Task.class);


        //备份
        TaskHis taskHis = new TaskHis();
        BeanUtils.copyProperties(task, taskHis);
        taskHis.setDeleteTime(new Date());

        //插入备份
        taskHisMapper.insert(taskHis);

        //删除任务表中的数据
        taskMapper.delete(task);

    }
}
