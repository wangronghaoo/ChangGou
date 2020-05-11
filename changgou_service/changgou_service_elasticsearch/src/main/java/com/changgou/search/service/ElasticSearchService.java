package com.changgou.search.service;

import java.util.Map;

public interface ElasticSearchService {
    void createIndex();

    void importAll();

    //根据id进行删除或者导入到es

    void importBySpuId(String spuId);

    void deleteBySpuId(String spuId);

    Map<String,Object> search(Map<String,String> paramMap);
}
