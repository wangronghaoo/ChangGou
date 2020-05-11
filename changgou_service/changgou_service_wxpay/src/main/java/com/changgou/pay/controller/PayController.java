package com.changgou.pay.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.pay.service.PayService;
import com.config.RabbitMqConfig;
import com.github.wxpay.sdk.WXPayUtil;
import com.utils.ConvertUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/wxpay")
public class PayController {

    @Autowired
    private PayService payService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RequestMapping("/nativePay")
    public Result nativePay(@RequestParam String orderId, @RequestParam Integer payMoney) {
        Map resultMap = payService.nativePay(orderId, payMoney);
        return new Result(true, StatusCode.OK, "调用微信支付成功", resultMap);
    }


    /**
     * 接收微信的通知
     */

    @RequestMapping("/notice")
    public void wxNotice(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("成功回调");
        try {
            String xml = ConvertUtils.convertToString(request.getInputStream());
            System.out.println(xml);
            //解析xml
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断
            if ("SUCCESS".equals(resultMap.get("result_code"))) {
                //调用微信api查询订单(订单号或者流水号2选1)
                Map orderResultMap = payService.queryOrder(resultMap.get("out_trade_no"));
                System.out.println("查询订单返回的结果" + orderResultMap);

                //websocket跳转支付结果页面
                rabbitTemplate.convertAndSend("paynotify", "", resultMap.get("out_trade_no"));

                //如果查询订单支付结果为success
                if ("SUCCESS".equals(orderResultMap.get("trade_state"))) {
                    //发送消息到mq,修改订单状态
                    Map mqMap = new HashMap();
                    mqMap.put("orderId", orderResultMap.get("out_trade_no"));
                    mqMap.put("transactionId", orderResultMap.get("transaction_id"));
                    rabbitTemplate.convertAndSend("", RabbitMqConfig.UpdateOrderQueue, JSON.toJSONString(mqMap));

                    //给微信一个成功的响应
                    response.setContentType("text/xml");
                    String data = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
                    response.getWriter().write(data);
                }
            } else {
                System.out.println(resultMap.get("err_code_des"));//错误信息描述
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 查询微信订单
     * @param orderId
     * @return
     */
    @GetMapping("/queryOrder")
    public Map queryOrder(@RequestParam String orderId) {
        Map map = payService.queryOrder(orderId);
        return map;

    }


    @RequestMapping("/closeOrder")
    public Map closeOrder(@RequestParam String out_trad_no){
        Map resultMap = payService.closeOrder(out_trad_no);
       return resultMap;
    }

/*
* <xml><appid><![CDATA[wx8397f8696b538317]]></appid>
<bank_type><![CDATA[OTHERS]]></bank_type>
<cash_fee><![CDATA[1]]></cash_fee>
<fee_type><![CDATA[CNY]]></fee_type>
<is_subscribe><![CDATA[N]]></is_subscribe>
<mch_id><![CDATA[1473426802]]></mch_id>
<nonce_str><![CDATA[PLTZqHa5vPXnHM6yAAcQ0dhR7wAOFAvU]]></nonce_str>
<openid><![CDATA[oNpSGwTzxAPFbOQhbvpmzrI8A19c]]></openid>
<out_trade_no><![CDATA[1225779420980187136]]></out_trade_no>
<result_code><![CDATA[SUCCESS]]></result_code>
<return_code><![CDATA[SUCCESS]]></return_code>
<sign><![CDATA[F601C7EEF49D8165FE7001A801C4795CFFAFBC987C3BCBBC22F0BBFDDF7EA45A]]></sign>
<time_end><![CDATA[20200207215346]]></time_end>
<total_fee>1</total_fee>
<trade_type><![CDATA[NATIVE]]></trade_type>
<transaction_id><![CDATA[4200000507202002079556723140]]></transaction_id>
</xml>*/
}
