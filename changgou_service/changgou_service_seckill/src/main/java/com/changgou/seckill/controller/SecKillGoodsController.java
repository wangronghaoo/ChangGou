package com.changgou.seckill.controller;


import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.seckill.config.TokenDecode;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/secKill")
public class SecKillGoodsController {

    @Autowired
    private SeckillService seckillService;


    @Autowired
    private TokenDecode tokenDecode;

    /**
     * 秒杀商品列表
     * @param nowTime
     * @return
     */
    @RequestMapping("/List")
    public Result findList(@RequestParam String nowTime){
        List<SeckillGoods> goods = seckillService.findList(nowTime);
        return new Result(true, StatusCode.OK,"查询成功",goods);
    }



    @RequestMapping("/addOrder")
    public Result addOrder(@RequestParam String time,@RequestParam Long id){

        //获取用户名
        String username = tokenDecode.getUserInfo().get("username");
        boolean result = seckillService.addOrder(username,time,id);
        if (result){
            return new Result(true, StatusCode.OK,"下单成功");
        }else {
            return new Result(true, StatusCode.OK,"下单失败");
        }

    }
}
