package com.changgou.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.CategoryFeign;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.service.PageService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageServiceImpl implements PageService {


    @Value("${pagepath}")
    private String pagePath;
    //种类
    @Autowired
    private CategoryFeign categoryFeign;

    //SKU
    @Autowired
    private SkuFeign skuFeign;

    //SPU
    @Autowired
    private SpuFeign spuFeign;


    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public void createHtml(String spuId){

        //1.创建Context对象，存储商品的相关数据
        Context context = new Context();
        //2.获取静态页面的相关数据
        Map<String,Object> itemData = getItemData(spuId);
        context.setVariables(itemData);
        //3获取文件路径
        File file = new File(pagePath);

        //4.如果文件不存在,则新建
        if (!file.exists()){
            file.mkdirs();
        }


        //5.定义文件输出流
        File file1 = new File(file +"/" + spuId + ".html");

        FileWriter fw = null;
        try {
            fw = new FileWriter(file1);
            //生成静态页面
            templateEngine.process("item",context,fw);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关闭资源
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    //获取页面相关信息
    private Map<String, Object> getItemData(String spuId) {

        Map<String, Object> resultMap = new HashMap<>();


        //获取sku数据
        List<Sku> skuList = skuFeign.findBySpuId(spuId);
        resultMap.put("skuList",skuList);


        //获取spu数据
        Spu spu = spuFeign.findSpuById(spuId).getData();
        resultMap.put("spu",spu);

        //获取图片数据
        if (spu != null){
            if (StringUtils.isNotEmpty(spu.getImages())){
                //多张图片,以逗号分隔
                resultMap.put("images",spu.getImages().split(","));
            }
        }

        //获取
        String specItems = spu.getSpecItems();
        Map specItemsMap = JSON.parseObject(specItems, Map.class);
        resultMap.put("specItemsMap",specItemsMap);

        //根据spuId获取种类
        //一级种类
        Category category1 = categoryFeign.findById(spu.getCategory1Id()).getData();
        resultMap.put("category1",category1);
        //二级种类
        Category category2 = categoryFeign.findById(spu.getCategory2Id()).getData();
        resultMap.put("category2",category2);
        //三级种类
        Category category3 = categoryFeign.findById(spu.getCategory3Id()).getData();
        resultMap.put("category3",category3);
        return resultMap;
    }
}
