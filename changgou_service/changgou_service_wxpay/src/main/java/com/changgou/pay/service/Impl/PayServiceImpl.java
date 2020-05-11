package com.changgou.pay.service.Impl;

import com.github.wxpay.sdk.WXPay;
import com.changgou.pay.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class PayServiceImpl implements PayService {


    @Autowired
    private WXPay wxPay;

    @Value("${pay.notifyUri}")
    private String notify_url;

    @Override
    public Map nativePay(String orderId, Integer payMoney) {

        /**
         * 调用微信支付接口
         */
        try {
            //参数map
            Map<String,String> dataMap = new HashMap<>();
            dataMap.put("body","畅购商城"); //商品描述
            dataMap.put("out_trade_no",orderId);  //订单号
            //使用double和float都会精度丢失
            //dataMap.put("total_fee", String.valueOf(payMoney * 100));  //以分为单位  金额
            BigDecimal money = new BigDecimal("0.01");  //真实money
            BigDecimal fen = money.multiply(new BigDecimal("100"));   //乘以100 = 1.00
            fen = fen.setScale(0,BigDecimal.ROUND_UP);  //向上取整 1.00 = 1 (微信不认识)
            dataMap.put("total_fee", String.valueOf(fen));
            dataMap.put("spbill_create_ip","127.0.0.1");
            dataMap.put("notify_url",notify_url);  //回调地址  通知地址
            dataMap.put("trade_type","NATIVE");  //支付方式
            //回调结果
            Map<String, String> resultMap = wxPay.unifiedOrder(dataMap);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询weixin订单
     * @param orderId
     * @return
     */
    @Override
    public Map queryOrder(String orderId) {
        try {
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("out_trade_no",orderId);
            Map resultMap = wxPay.orderQuery(dataMap);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 关闭订单
     * @param out_trad_no
     * @return
     */
    @Override
    public Map closeOrder(String out_trad_no) {

        try {
            Map dataMap = new HashMap();
            dataMap.put("out_trad_no",out_trad_no);
            Map map = wxPay.closeOrder(dataMap);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


}
