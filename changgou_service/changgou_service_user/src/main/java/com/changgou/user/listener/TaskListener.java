package com.changgou.user.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.config.MqConfig;
import com.changgou.order.pojo.Task;
import com.changgou.user.dao.PrintLogMapper;
import com.changgou.user.dao.UserMapper;
import com.changgou.user.pojo.PointLog;
import com.changgou.user.pojo.User;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class TaskListener {


    @Autowired
    private RedisTemplate redisTemplate;


    @Autowired
    private PrintLogMapper printLogMapper;


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = MqConfig.AddPointQueue)
    public void updatePoint(String message){
        //message:task
        Task task = JSON.parseObject(message, Task.class);

        if(task == null || StringUtils.isEmpty(task.getRequestBody())){
            return;
        }

        if (redisTemplate.hasKey(task.getId())){
            //说明正在执行
            return;
        }

        //没有判断插入积分操作是否完成
        Map map = JSON.parseObject(task.getRequestBody(), Map.class);
        String orderId = (String) map.get("orderId");
        String username = (String) map.get("username");
        Integer point = (Integer) map.get("point");
        //查询是否存在该订单
        PointLog byPrimaryKey = printLogMapper.selectByPrimaryKey(orderId);
        if (byPrimaryKey != null){
            return;
        }

        //存入redis中  30s
        redisTemplate.boundValueOps(task.getId()).set(username,30, TimeUnit.SECONDS);
        //更新积分
        User user = userMapper.selectByPrimaryKey(username);
        user.setPoints(user.getPoints() + point);
        userMapper.updateByPrimaryKey(user);

        //插入积分日志
        PointLog newPointLog = new PointLog();
        newPointLog.setOrderId(orderId);
        newPointLog.setPoint(point);
        newPointLog.setUserId(user.getUsername());
        printLogMapper.insertSelective(newPointLog);
        //删除redis中的数据
        redisTemplate.delete(task.getId());

        //将task发送到添加积分完成队列,然后删除数据库中的数据
        rabbitTemplate.convertAndSend(MqConfig.AddPointExchange,MqConfig.FinishAddPointRouteKey,JSON.toJSONString(task));
    }


}
