package com.changgou.canan.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.canan.RabbitMq.SpuRabbitMqConfig;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@CanalEventListener
public class SpuListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @ListenPoint(schema = "changgou_goods",table = "tb_spu")
    public void upGoods(CanalEntry.EventType eventType,CanalEntry.RowData rowData){

        String beforeValue = "";   //上架之前
        String afterValue = "";    //上架之后
        String spu_Id = "";       //spuId


        //获取修改之前的数据
        List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
        for (CanalEntry.Column column : beforeColumnsList) {
            if ("is_marketable".equals(column.getName())){
                beforeValue=column.getValue();
            }
        }
        //获取修改之后的数据,并且发送到rabbitmq
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        for (CanalEntry.Column column : afterColumnsList) {
            if ("is_marketable".equals(column.getName())){
                afterValue=column.getValue();
            }

            //获取spu的id
            if ("id".equals(column.getName())){
                spu_Id=column.getValue();
                System.out.println(spu_Id);
            }
        }

        //    上架的定义    0 -> 1
        if ("0".equals(beforeValue) && "1".equals(afterValue)){
            //发送rabbitmq
            rabbitTemplate.convertAndSend(SpuRabbitMqConfig.SPU_UPANDDOWN_EXCHANGE,"spu_up_routekey",spu_Id);
            System.out.println("发送成功");
        }


        //下架
        if ("1".equals(beforeValue) && "0".equals(afterValue)){
            //发送rabbitmq
            rabbitTemplate.convertAndSend(SpuRabbitMqConfig.SPU_UPANDDOWN_EXCHANGE,"spu_down_routekey",spu_Id);
            System.out.println("发送成功");
        }

    }
}
