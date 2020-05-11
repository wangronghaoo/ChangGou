package com.changgou.search.controller;


import com.alibaba.fastjson.JSON;
import com.changgou.entity.Page;
import com.changgou.pojo.SkuInfo;
import com.changgou.search.service.ElasticSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private ElasticSearchService elasticSearchService;

    @GetMapping
    @ResponseBody
    public Map searchSku(@RequestParam Map<String, String> paramMap) {

        //根据关键词进行查询
        if (paramMap != null && paramMap.size() > 0) {
            return elasticSearchService.search(paramMap);
        }
        return null;
    }


    @GetMapping("/list")
    public String search(@RequestParam Map<String, String> searchMap, Model model) {

        /*if (searchMap ==null || searchMap.size() < 0){
            return "search";
        }*/
        Map<String, Object> resultMap = elasticSearchService.search(searchMap);
        model.addAttribute("searchMap", searchMap);
        model.addAttribute("resultMap", resultMap);

        //拼接url
        StringBuilder url = new StringBuilder("/search/list");
        if (searchMap != null && searchMap.size() > 0) {
            url.append("?");
            Set<String> set = searchMap.keySet();
            //拼接 键,值
            for (String key : set) {
                if (!"pageNum".equals(key) && !"sortByField".equals(key) && !"sortByRule".equals(key)) {
                    String value = searchMap.get(key);
                    url.append(key).append("=").append(value).append("&");
                }
            }
        }

        String urlString = url.toString();
        urlString = urlString.substring(0, urlString.length() - 1);
        model.addAttribute("url", urlString);


        //分页
      Page<SkuInfo> page = new Page<SkuInfo>(Long.parseLong(resultMap.get("total").toString()),Integer.parseInt(resultMap.get("pageNum").toString()),Integer.parseInt(resultMap.get("pageSize").toString()));
      model.addAttribute("page",page);

      return "search";
    }
}
