package com.changgou.search.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.search.service.ElasticSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
public class ElasticManager {

    @Autowired
    private ElasticSearchService elasticSearchService;


    /**
     * 创建索引库
     * @return
     */

    @PostMapping("/createIndex")
    public Result createIndex(){
        elasticSearchService.createIndex();
        return new Result(true, StatusCode.OK,"创建索引库成功");
    }

    /**
     * 导入数据
     */
    @GetMapping("/importData")
    public Result importAll(){
        elasticSearchService.importAll();
        return new Result(true, StatusCode.OK,"导入所有数据成功");
    }


}
