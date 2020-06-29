package com.zx.controller;

import com.zx.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author ZhangXiong
 * @version v12.0.1
 * @date 2020-06-28
 */
@RestController
public class ContentController {
    @Autowired
    private ContentService contentService;

    @GetMapping("/parse/{keyword}")
    public Boolean parse(@PathVariable("keyword") String keyword) throws Exception{
        return contentService.parseContent(keyword);
    }

    @GetMapping("/search/{keyword}/{pageNo}/{pageSize}")
    public List<Map<String,Object>> search(@PathVariable("keyword")String keyword,
                                           @PathVariable("pageNo")int pageNo,
                                           @PathVariable("pageSize")int pageSize) throws Exception {
        return contentService.searchPageHighlightBuilder(keyword,pageNo,pageSize);
    }
}
