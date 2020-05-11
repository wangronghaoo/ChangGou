package com.changgou.goods.dao;

import com.changgou.goods.pojo.Brand;
import com.changgou.goods.pojo.Spec;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand> {

    @Select("SELECT * FROM tb_brand WHERE id IN (SELECT cb.brand_id FROM tb_category_brand cb WHERE cb.category_id IN (SELECT c.`id` FROM tb_category c WHERE NAME = #{brandName}))")
    List<Brand> findByCategoryBrand(String brandName);


    @Select("SELECT * FROM tb_spec sp WHERE sp.`template_id` IN (SELECT t.id FROM tb_template t WHERE id IN (SELECT c.template_id FROM tb_category c WHERE c.name = #{categoryName}))")
    List<Spec> findSpecByCategory(String categoryName);
}
