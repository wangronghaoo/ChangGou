package com.changgou.search.service.Impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.pojo.SkuInfo;
import com.changgou.search.service.ElasticSearchService;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsMapper;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ElasticSearchImpl implements ElasticSearchService {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private SkuFeign skuFeign;


    @Autowired
    private ElasticsearchRepository elasticsearchRepository;
    private NativeSearchQueryBuilder nativeSearchQueryBuilder;

    @Override
    public void createIndex() {
        elasticsearchTemplate.createIndex(SkuInfo.class);
        elasticsearchTemplate.putMapping(SkuInfo.class);
    }

    @Override
    public void importAll() {
        //使用feign调用具体的微服务
        //导入全部数据
        List<Sku> skus = skuFeign.findBySpuId("all");

        if (skus == null || skus.size() <= 0){
            throw  new RuntimeException("没有数据可以导入...");
        }

        //将数据转化为json
        String string = JSON.toJSONString(skus);
        //将字符串转化成指定的pojo
        List<SkuInfo> skuInfos = JSON.parseArray(string, SkuInfo.class);
        elasticsearchRepository.saveAll(skuInfos);
    }

    /**
     * 上架商品
     * @param spuId
     */
    @Override
    public void importBySpuId(String spuId) {
        List<Sku> skuListBySpuId = skuFeign.findBySpuId(spuId);
        //将数据转为指定类
        String string = JSON.toJSONString(skuListBySpuId);
        List<SkuInfo> skuInfos = JSON.parseArray(string, SkuInfo.class);
        elasticsearchRepository.saveAll(skuInfos);
    }

    /**
     * 下架商品
     * @param id
     */
    @Override
    public void deleteBySpuId(String id) {
        //根据id查找出所有的sku
        List<Sku> skuListBySpuId = skuFeign.findBySpuId(id);
        //遍历所有sku
        for (Sku sku : skuListBySpuId) {
        //根据sku的id进行删除
            elasticsearchRepository.deleteById(Long.parseLong(sku.getId()));
        }
    }


    /**
     * 根据条件进行查询
     * @param paramMap
     * @return
     */
    @Override
    public Map<String,Object> search(Map<String,String> paramMap) {


        //返回的结果map
        Map<String,Object> resultMap = new HashMap<>();

        //关键词是否为空
       if (StringUtils.isEmpty(paramMap.get("keyword"))){
           return null;
       }

        //根据关键词进行查询
        String keyword = paramMap.get("keyword");


       //关键词  匹配查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("name", keyword));


        //品牌查询
        if(StringUtils.isNotEmpty(paramMap.get("brandName"))){
            boolQueryBuilder.must(QueryBuilders.termQuery("brandName",paramMap.get("brandName")));
        }


        //规格过滤
        Set<Map.Entry<String, String>> entries = paramMap.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            String key = entry.getKey();
            //如果包含spec_则为规格
            if (key.startsWith("spec_")){
                //获取对应的值
                String value = paramMap.get(key);
                System.out.println(value);
                System.out.println(key.substring(5));
                boolQueryBuilder.must(QueryBuilders.termQuery("spec." + key.substring(5),value));
            }
        }


        //价格条件
        if (StringUtils.isNotEmpty(paramMap.get("price"))) {
            String[] prices = paramMap.get("price").split("-");
            if (prices.length == 2){
                //大于最小值,小于最大值
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lte(prices[1]).gte(prices[0]));
            }
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(prices[0]));
        }

        NativeSearchQueryBuilder nativeSearchQueryBuilder= new NativeSearchQueryBuilder().withQuery(boolQueryBuilder);

        //高亮展示
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");
        field.preTags("<span style='color:red'>");
        field.postTags("</span>");

        nativeSearchQueryBuilder.withHighlightFields(field);


        //定义品牌结果列名
        String skuBrand = "skuBrand";

        //添加条件  AggregationBuilders.terms(结果列列名).filed(分组域名)
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuBrand).field("brandName"));

        //定义规格列名称
        String skuSpec = "skuSpec";

        //添加规格条件
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuSpec).field("spec"));


        //排序
        //前端接收要排序的顺序,以及字段
        if (StringUtils.isNotEmpty(paramMap.get("sortByField"))){
            if (paramMap.get("sortByRule").equals("ASC")){
                nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(paramMap.get("sortByField")).order(SortOrder.ASC));
            } else if (paramMap.get("sortByRule").equals("DESC")) {
                nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(paramMap.get("sortByField")).order(SortOrder.DESC));
            }
        }

        //分页查询
        String pageNum = paramMap.get("pageNum");
        if (pageNum == null){
            pageNum = "1";
        }

        String pageSize = paramMap.get("pageSize");
        if (pageSize == null){
            pageSize = "20";
        }
        //当前页
        resultMap.put("pageNum",pageNum);
        resultMap.put("pageSize",pageSize);

        nativeSearchQueryBuilder.withPageable(PageRequest.of(Integer.parseInt(pageNum) - 1,Integer.parseInt(pageSize)));

        //查询结果
        //手动解析高亮区域
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class, new SearchResultMapper(){
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {

                List<T> list = new ArrayList<>();
                //返回的结果
                SearchHits hits = searchResponse.getHits();
                if (hits != null){
                    for (SearchHit hit : hits) {
                        //转为指定的格式
                        SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);
                        Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                        if (highlightFields != null && highlightFields.size() > 0){
                            skuInfo.setName(highlightFields.get("name").getFragments()[0].toString());
                            list.add((T) skuInfo);
                        }
                    }
                }
                return new AggregatedPageImpl<T>(list,pageable,hits.getTotalHits(),searchResponse.getAggregations());
            }
        });
        //总条数
        resultMap.put("total",aggregatedPage.getTotalElements());
        //总页数
        resultMap.put("totalPages",aggregatedPage.getTotalPages());

        resultMap.put("rows",aggregatedPage.getContent());

        //获取品牌聚合结果
        StringTerms aggregation = (StringTerms) aggregatedPage.getAggregation(skuBrand);

        //获得规格聚合结果
        StringTerms skuSpecs = (StringTerms) aggregatedPage.getAggregation(skuSpec);

        //品牌结果
        List<String> brandList = aggregation.getBuckets().stream().map(StringTerms.Bucket::getKeyAsString).collect(Collectors.toList());

        resultMap.put("brandList",brandList);

        List<String> specList = new ArrayList<>();
        //规格结果
        List<StringTerms.Bucket> buckets = skuSpecs.getBuckets();
        for (StringTerms.Bucket bucket : buckets) {
            String spec = bucket.getKeyAsString();
            specList.add(spec);
        }

        resultMap.put("specList",this.formatSpec(specList));
        return resultMap;
    }


    //商品属性格式  将specList转为map  [颜色:{黑色.金色...},规格:{...},尺寸:{....}]
    private Map<String, Set<String>> formatSpec(List<String> specList) {

        Map<String, Set<String>> map = new HashMap<>();

        if (specList != null && specList.size() > 0) {
            for (String spec : specList) {
                //将list转为map
                Map<String, String> specMap = JSON.parseObject(spec, Map.class);
                //获取所有的键
                Set<String> set = specMap.keySet();
                for (String specKey : set) {
                    //获取对应的值
                    String value = specMap.get(specKey);
                    //没有,则添加键,value
                    if (!map.containsKey(specKey)) {
                        Set<String> specValueSet = new HashSet<>();
                        specValueSet.add(value);
                        map.put(specKey, specValueSet);
                    }else {
                        //添加value
                        Set<String> strings = map.get(specKey);
                        strings.add(value);
                    }
                }
            }
        }
        return map;
    }

}
