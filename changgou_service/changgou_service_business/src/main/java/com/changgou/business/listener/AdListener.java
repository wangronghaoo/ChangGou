package com.changgou.business.listener;

import com.alibaba.fastjson.JSON;
import okhttp3.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import sun.net.www.http.HttpClient;

import java.io.IOException;


@Component
public class AdListener {

    @Autowired
    private RestTemplate restTemplate;

    //监听广告更新队列的消息
    //发送http请求:OkHttpClient,HttpClient  RestTemplate

    @RabbitListener(queues = "ad_update_queue")
    public void receiveMessage(String message){
        System.out.println(message);
        String uri = "http://192.168.200.128/ad_update?position="+message;
        String result = restTemplate.getForObject(uri, String.class);
        System.out.println(result);       //{"flag":true,"position":"web_index_lb"}




//        OkHttpClient okHttpClient = new OkHttpClient();
//        String uri = "http://192.168.200.128/ad_update?position="+message;
//        Request request = new Request.Builder().url(uri).build();
//        Call call = okHttpClient.newCall(request);
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                //请求失败
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                System.out.println("请求成功:" + response.message());
//            }
//        });

    }
}
