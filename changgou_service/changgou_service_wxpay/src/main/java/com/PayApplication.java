package com;

import com.github.wxpay.sdk.MyWxPayConfig;
import com.github.wxpay.sdk.WXPay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient
public class PayApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class);
    }


    @Bean
    public WXPay wxPay(){
        try {
            MyWxPayConfig wxPayConfig = new MyWxPayConfig();
            return new WXPay(wxPayConfig);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
