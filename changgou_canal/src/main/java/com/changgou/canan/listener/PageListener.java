package com.changgou.canan.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.canan.RabbitMq.SpuRabbitMqConfig;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@CanalEventListener
public class PageListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @ListenPoint(schema = "changgou_goods",table = "tb_spu")
    public void upGoods(CanalEntry.EventType eventType, CanalEntry.RowData rowData){

        String beforeStatus = "";
        String afterStatus = "";
        String spuId ="";
        //获取修改之前的数据
        List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
        for (CanalEntry.Column column : beforeColumnsList) {
            if ("status".equals(column.getName())){
                beforeStatus=column.getValue();
            }
        }


        //获取修改之后的数据,并且发送到rabbitmq
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        for (CanalEntry.Column column : afterColumnsList) {
            if ("status".equals(column.getName())){
                afterStatus=column.getValue();
            }
            //获取spu的id
            if ("id".equals(column.getName())){
                spuId=column.getValue();
                System.out.println(spuId);
            }


            //审核通过
            if ("0".equals(beforeStatus) && "1".equals(afterStatus)){
                //发送rabbitmq
                rabbitTemplate.convertAndSend(SpuRabbitMqConfig.CREATE_PAGE_EXCHANGE,"create_page_routekey",spuId);
                System.out.println("发送成功");
            }

        }
    }
}
