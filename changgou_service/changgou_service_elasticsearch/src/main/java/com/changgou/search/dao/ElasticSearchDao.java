package com.changgou.search.dao;


import com.changgou.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticSearchDao extends ElasticsearchRepository<SkuInfo,Long> {
}
