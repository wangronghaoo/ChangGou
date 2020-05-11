package com.changgou.goods.dao;

import com.changgou.goods.pojo.Sku;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface SkuMapper extends Mapper<Sku> {

    @Update("update tb_sku set num = num-#{num},sale_num = sale_num + #{num} where id = #{skuId} and num >= #{num}")
    int updateCount(String skuId, Integer num);


}
