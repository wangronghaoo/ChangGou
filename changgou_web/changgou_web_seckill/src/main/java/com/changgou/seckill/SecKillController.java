package com.changgou.seckill;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.seckill.feign.SeckillFeign;
import com.changgou.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Controller
@RequestMapping("/wsecKill")
public class SecKillController {

    //商品+用户,防止恶意刷单
    private final String SecKillOrder = "SecKillOrder_";

    @Autowired
    private SeckillFeign seckillFeign;


    @Autowired
    private RedisTemplate redisTemplate;


    @RequestMapping("/toIndex")
    public String toIndex(){
        return "seckill-index";
    }

    @GetMapping("/dateMenus")
    @ResponseBody
    public Result dates(){

        //返回结果集
        List<String> result = new ArrayList<>();

        //日期结果
        List<Date> dateMenus = DateUtil.getDateMenus();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Date dateMenu : dateMenus) {
            String format = sdf.format(dateMenu);
            result.add(format);
        }

        return new Result(true, StatusCode.OK,"时间段查询成功",result);
    }

    @RequestMapping("/goodList")
    @ResponseBody
    public Result goodsList(String time) throws ParseException {
        System.out.println(time);  //2020-02-11 20:00:00
        //将格式转化为yyyyMMddHH

        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
        String nowTime = sdf.format(date);
        return seckillFeign.findList(nowTime);

    }


    @RequestMapping("/add")
    @ResponseBody
    public Result addOrder(@RequestParam String time, @RequestParam Long id) throws ParseException {

        //判断该用户是否重复点击
        Object o = redisTemplate.boundValueOps(SecKillOrder + id).get();
        if (o != null){
            //已经重复
            return new Result(false,StatusCode.ERROR,"五分钟之后再试!!!");
        }

        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
        time = sdf.format(date);
        Result result = seckillFeign.addOrder(time,id);
        return result;
    }


}
