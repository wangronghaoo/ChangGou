package com.changgou.search.listener;

import com.changgou.search.service.ElasticSearchService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpuListener {


    @Autowired
    private ElasticSearchService searchService;
    //上架商品的id
    @RabbitListener(queues = "spu_up_queue")
    public void receiveUpMessage(String spuId){
        System.out.println(spuId);
        searchService.importBySpuId(spuId);
        System.out.println("上架成功");
    }




    //下架商品的id
    @RabbitListener(queues = "spu_down_queue")
    public void receiveDownMessage(String spuId){
        System.out.println(spuId);
        searchService.deleteBySpuId(spuId);
        System.out.println("下架成功");
    }
}
