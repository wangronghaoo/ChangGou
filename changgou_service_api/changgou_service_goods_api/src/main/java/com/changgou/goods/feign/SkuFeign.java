package com.changgou.goods.feign;

import com.changgou.entity.Result;
import com.changgou.goods.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "goods") //指定服务名称
public interface SkuFeign {

    @GetMapping("/sku/spu/sku/{spuId}")
    List<Sku> findBySpuId(@PathVariable("spuId") String spuId);



    /***
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping("/sku/{id}")
    Result<Sku> findById(@PathVariable String id);


    /**
     * 修改库存数量
     */
    @PostMapping("/sku/decrCount")
    void updateCount(@RequestParam("username") String username);


    /**
     * 回退库存
     * @param skuId
     */
    @PutMapping("/sku/addCount")
    void addNum(@RequestParam String skuId,@RequestParam Integer num);
}
