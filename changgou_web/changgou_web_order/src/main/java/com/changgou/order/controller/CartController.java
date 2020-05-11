package com.changgou.order.controller;


import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.order.feign.OrderFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/wcart")
public class CartController {

    @Autowired
    private OrderFeign orderFeign;



    /**
     * 带数据返回cart
     * @param
     * @return
     */
    @GetMapping("/cart")
    public String cart(){
        return "cart";
    }

    /**
     * 查询购物车
     */

    @GetMapping("/list")
    @ResponseBody
    public Result cartResult(){
        Map<String,Object> data = orderFeign.list().getData();
        return new Result(true,StatusCode.OK,"查询成功",data);
    }


    /**
     * 添加购物车
     *
     */
    @GetMapping("/add")
    @ResponseBody
    public Result add(@RequestParam("skuId") String skuId , @RequestParam("num") Integer num){
        return orderFeign.addCart(skuId, num);

    }


    @GetMapping("/delete")
    @ResponseBody
    public Result deleteCart(@RequestParam("skuId") String id){
        return orderFeign.delete(id);
    }

}
