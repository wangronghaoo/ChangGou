package com.changgou.Listener;

import com.changgou.service.PageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PageListener {


    @Autowired
    private PageService pageService;


    @RabbitListener(queues = "create_page_queue")
    public void receiveMessage(String spuId){
        try {
            System.out.println(spuId);
            pageService.createHtml(spuId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
