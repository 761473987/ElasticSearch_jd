package com.zy.es.controller;

import com.zy.es.service.ContentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author liuzhiyun
 * @version v12.0.1
 * @date 2020-10-23
 */
@RestController
@RequestMapping("Elasticsearch")
@Slf4j
public class ContentController {
    @Autowired
    private ContentService contentService;

    @GetMapping("/parse/{keyword}")
    public String parse(@PathVariable("keyword") String keyword) throws Exception{
        return contentService.parseContent(keyword);
    }

    @GetMapping("/search/{keyword}/{pageNo}/{pageSize}")
    public List<Map<String,Object>> search(@PathVariable("keyword")String keyword,
                                           @PathVariable("pageNo")int pageNo,
                                           @PathVariable("pageSize")int pageSize) throws Exception {
        long start = System.currentTimeMillis();
        List<Map<String,Object>> mapList =  contentService.searchPageHighlightBuilder(keyword,pageNo,pageSize);
        long end = System.currentTimeMillis();
        log.info("=======Elasticsearch花费的时间是：{}=======", end-start);
        return mapList;
    }
}
