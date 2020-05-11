package com.changgou.canan.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.canan.RabbitMq.AdRabbitMqConfig;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

@CanalEventListener  //当前类是canal的监听类
public class BusinessListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     *
     * @param eventType  当前操作数据库的类型
     * @param rowData    当前操作数据库的数据
     */

    @ListenPoint(schema = "changgou_business",table = "tb_ad")
    public void adUpdate(CanalEntry.EventType eventType,CanalEntry.RowData rowData){
        System.out.println("广告数据发送改变");

        //获得改变之前的数据
        rowData.getBeforeColumnsList().forEach((column -> System.out.println("改变之前的数据:" + column.getName() + "::" + column.getValue())));
        System.out.println("=====================");

        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            if ("position".equals(column.getName())){
                System.out.println("发送数据到rabbitMq:" + column.getValue());
                //发送给rabbitmq队列
                rabbitTemplate.convertAndSend("", AdRabbitMqConfig.AD_UPDATE_QUEUE,column.getValue());
            }
        }

        //获取改变之后的数据
        rowData.getAfterColumnsList().forEach((column -> System.out.println("改变之后的数据为:" + column.getName() + "::" + column.getValue())));


    }
}
