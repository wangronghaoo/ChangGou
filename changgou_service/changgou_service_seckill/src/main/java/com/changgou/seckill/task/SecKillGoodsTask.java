package com.changgou.seckill.task;

import com.changgou.seckill.dao.SeckillMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SecKillGoodsTask {

    @Autowired
    private SeckillMapper seckillMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    //秒杀商品redis大键
    private final String SecKillGoodsKey = "SecKillGoodsKey_";

    //库存数量
    private final String SecKillGoodsCount = "SecKillGoodsCount_";
    /*1.查询所有符合条件的秒杀商品
            1) 获取时间段集合并循环遍历出每一个时间段
            2) 获取每一个时间段名称,用于后续redis中key的设置
            3) 状态必须为审核通过 status=1
            4) 商品库存个数>0
            5) 秒杀商品开始时间>=当前时间段
            6) 秒杀商品结束<当前时间段+2小时
            7) 排除之前已经加载到Redis缓存中的商品数据
            8) 执行查询获取对应的结果集
      2.将秒杀商品存入缓存

     */

    //每分钟的第0s开始
    @Scheduled(cron = "0 * * * * ?")
    public void secKillGoodsList(){

        List<Date> dateMenus = DateUtil.getDateMenus();
        // 00:00:00 - 24:00:00
        for (Date dateMenu : dateMenus) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            //将日期值作为大key存入redis中
            String formatDate = DateUtil.date2Str(dateMenu);  //yyyyMMddHH
            //设置查询条件
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("status",1);
            criteria.andGreaterThan("stockCount",0);
            criteria.andGreaterThanOrEqualTo("startTime",sdf.format(dateMenu));
            criteria.andLessThan("endTime",sdf.format(DateUtil.addDateHour(dateMenu,2)));


            //排除之前已经加载到Redis缓存中的商品数据
            Set keys = redisTemplate.boundHashOps(SecKillGoodsKey + formatDate).keys();
            if (keys != null && keys.size() > 0){
                criteria.andNotIn("id",keys);
            }

            //根据条件查询
            List<SeckillGoods> seckillGoods = seckillMapper.selectByExample(example);
            for (SeckillGoods seckillGood : seckillGoods) {
                System.out.println("在" + formatDate + "的秒杀商品有" + seckillGood.toString());
                //将查询出来的商品存入redis中(20200210(goodId,good))
                redisTemplate.opsForHash().put(SecKillGoodsKey + formatDate,seckillGood.getId(),seckillGood);
                //将剩余库存加到redis中
                redisTemplate.boundValueOps(SecKillGoodsCount + seckillGood.getId()).set(seckillGood.getStockCount());
            }
        }
    }





}
