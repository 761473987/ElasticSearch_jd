package com.zy.es.service;

import com.alibaba.fastjson.JSON;
import com.zy.es.pojo.Content;
import com.zy.es.pojo.JdGoods;
import com.zy.es.utils.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author liuzhiyun
 * @version v12.0.1
 * @date 2020-10-23
 */
// 业务编写
@Service
public class ContentService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    
    @Autowired
    private IJdGoodsService goodsService;


    // 1.解析数据放入es搜索中
    public String parseContent(String keywords) throws Exception{
        List<Content> contents = new HtmlParseUtil().parseJD(keywords);
        // 把查询到的数据放入es中
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("2m");
        List<JdGoods> goodsList = new ArrayList<JdGoods>();
        for (int i = 0; i < contents.size(); i++) {
            bulkRequest.add(new IndexRequest("jd_goods")
            .source(JSON.toJSONString(contents.get(i)),XContentType.JSON));
            //填充到数据实体类
            JdGoods goods = new JdGoods();
            BeanUtils.copyProperties(contents.get(i), goods);
            goodsList.add(goods);
        }
        
        //填充到数据库
        goodsService.saveBatch(goodsList);
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
//        return !bulk.hasFailures();
        return !bulk.hasFailures() ? "填充数据成功！" : "填充数据失败！";
    }

    // 2.获取这些数据实现搜索功能
    public List<Map<String,Object>> searchPage(String keyword,int pageNo,int pageSize) throws Exception {
        if(pageNo <= 1){
            pageNo = 1;
        }
        // 条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);

        
        // 精准匹配
//        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);
//        sourceBuilder.query(termQueryBuilder);
        //模糊匹配
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("title", keyword);
        sourceBuilder.query(queryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // 执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        // 解析结果
        List<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit documentFields : search.getHits().getHits()) {
            list.add(documentFields.getSourceAsMap());
        }
        return list;
    }

    // 3.获取这些数据实现搜索高亮功能
    public List<Map<String,Object>> searchPageHighlightBuilder(String keyword,int pageNo,int pageSize) throws Exception {
        if(pageNo <= 1){
            pageNo = 1;
        }
        // 条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);

        // 精准匹配
//        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);
//        sourceBuilder.query(termQueryBuilder);
        //模糊匹配
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("title", keyword);
        sourceBuilder.query(queryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // 高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        // 关闭多个高亮
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);


        // 执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        // 解析结果
        List<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit hit : search.getHits().getHits()) {
            //解析高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            if(title != null){
                Text[] fragments = title.fragments();
                String n_title = "";
                for (Text text : fragments) {
                    n_title += text;
                }
                // 高亮字段替换掉原来的内容即可
                sourceAsMap.put("title",n_title);
            }
            list.add(sourceAsMap);
        }
        return list;
    }
}
