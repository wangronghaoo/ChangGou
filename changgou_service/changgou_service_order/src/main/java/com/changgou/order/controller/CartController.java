package com.changgou.order.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.order.config.TokenDecode;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private TokenDecode tokenDecode;

    @GetMapping("/addCart")
    public Result addCart(@RequestParam("skuId") String skuId, @RequestParam("num") Integer num){

        //动态获取当前人信息,暂时静态
        String username = tokenDecode.getUserInfo().get("username");
        cartService.add(username,skuId,num);
        return new Result(true, StatusCode.OK,"加入购物车成功");
    }

    @GetMapping("/list")
    public Result<Map> list(){
        //动态获取当前人信息,暂时静态
        //String username = "itcast";
        String username = tokenDecode.getUserInfo().get("username");
        Map map = cartService.cartList(username);
        return new Result<>(true,StatusCode.OK,"查询成功",map);
    }

    @RequestMapping("/delete")
    public Result delete(@RequestParam String skuId){
        String username = tokenDecode.getUserInfo().get("username");
        cartService.delete(username,skuId);
        return new Result(true,StatusCode.OK,"删除成功");
    }
}
