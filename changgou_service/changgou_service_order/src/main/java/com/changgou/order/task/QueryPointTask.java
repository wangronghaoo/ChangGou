package com.changgou.order.task;

import com.alibaba.fastjson.JSON;
import com.changgou.order.dao.TaskMapper;
import com.changgou.order.pojo.Task;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Component
public class QueryPointTask {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //查询当前任务库中的数据
    @Scheduled(cron = "0/2 * * * * ?")
    public void findTask(){
        Example example = new Example(Task.class);
        Date date = new Date();
        long dateTime = date.getTime();
        dateTime += 1000 * 60 * 60 * 8;
        date.setTime(dateTime);
        example.createCriteria().andLessThan("updateTime",date);
        List<Task> taskList = taskMapper.selectByExample(example);
        //将消息发送到rabbitmq
        if (taskList != null && taskList.size() > 0){
            for (Task task : taskList) {
                System.out.println("将任务表中的数据发送到rabbitMq中" + task.getMqRoutingkey());
                rabbitTemplate.convertAndSend(task.getMqExchange(),task.getMqRoutingkey(), JSON.toJSONString(task));
            }
        }
    }





}
