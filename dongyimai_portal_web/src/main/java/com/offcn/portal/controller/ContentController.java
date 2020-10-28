package com.offcn.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.content.service.ContentService;
import com.offcn.pojo.TbContent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Auther: ysp
 * @Date: 2020/9/29 14:20
 * @Description:  广告管理的控制器
 */
@RestController
@RequestMapping("/content")
public class ContentController {

    @Reference
    private ContentService contentService;

    @RequestMapping("/findByCategoryId")
    public List<TbContent> findByCategoryId(Long categoryId){
        return contentService.findByCategoryId(categoryId);
    }

}
