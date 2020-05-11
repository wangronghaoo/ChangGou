package com.changgou.pay.service;

import java.util.Map;

public interface PayService {
    Map nativePay(String orderId, Integer payMoney);

    Map queryOrder(String out_trade_no);

    Map closeOrder(String out_trad_no);

}
