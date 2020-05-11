package com.changgou.order.task;

import com.changgou.order.dao.OrderMapper;
import com.changgou.order.pojo.Order;
import com.changgou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class AutoTake {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderService orderService;

    //需求：如果订单发货时间已经超过15天，用户没有进行确认收货，完成自动收货；

    /*
    * 0 0 10,14,16 * * ? 每天上午10点，下午2点，4点
    0 0/30 9-17 * * ? 朝九晚五工作时间内每半小时
    0 0 12 ? * WED 表示每个星期三中午12点
    "0 0 12 * * ?" 每天中午12点触发
    "0 15 10 ? * *" 每天上午10:15触发
    "0 15 10 * * ?" 每天上午10:15触发
    "0 15 10 * * ? *" 每天上午10:15触发
    "0 15 10 * * ? 2005" 2005年的每天上午10:15触发
    "0 * 14 * * ?" 在每天下午2点到下午2:59期间的每1分钟触发
    "0 0/5 14 * * ?" 在每天下午2点到下午2:55期间的每5分钟触发
    "0 0/5 14,18 * * ?" 在每天下午2点到2:55期间和下午6点到6:55期间的每5分钟触发
    "0 0-5 14 * * ?" 在每天下午2点到下午2:05期间的每1分钟触发
    "0 10,44 14 ? 3 WED" 每年三月的星期三的下午2:10和2:44触发
    "0 15 10 ? * MON-FRI" 周一至周五的上午10:15触发
    "0 15 10 15 * ?" 每月15日上午10:15触发
    "0 15 10 L * ?" 每月最后一日的上午10:15触发
    "0 15 10 ? * 6L" 每月的最后一个星期五上午10:15触发
    "0 15 10 ? * 6L 2002-2005" 2002年至2005年的每月的最后一个星期五上午10:15触发
    "0 15 10 ? * 6#3" 每月的第三个星期五上午10:15触发*/


    //从0秒开始每隔2s扫描一次
    @Scheduled(cron = "0/2 * * * * ?")
    public void autoTake(){
        //获取比当前日期小于15天的日期
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH,-15);
        Date date = calendar.getTime();
        System.out.println(date);
        //发货日期 < 当前时间 -15
        Example example = new Example(Order.class);
        example.createCriteria().andLessThanOrEqualTo("consignTime",date);
        //订单状态为已完成的
        example.createCriteria().andEqualTo("orderStatus","2");

        //未进行收货的
        List<Order> orders = orderMapper.selectByExample(example);
        //自动收货
        for (Order order : orders) {
            System.out.println("未进行手动收货的" + order.getId());
            orderService.confirmOrder(order.getId(),"system");
        }

    }

}
