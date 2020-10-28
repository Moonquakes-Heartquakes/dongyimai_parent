package com.offcn.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.content.service.ContentService;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbContentMapper;
import com.offcn.pojo.TbContent;
import com.offcn.pojo.TbContentExample;
import com.offcn.pojo.TbContentExample.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private TbContentMapper contentMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询全部
     */
    @Override
    public List<TbContent> findAll() {
        return contentMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbContent content) {
        //1.先清空缓存记录
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());
        contentMapper.insert(content);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbContent content) {
        //1.清空进行修改分类下的缓存
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());

        //2.查询修改之前的分类
        Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
        //3.比较原有分类和表单修改的分类，如果不一致，则将原有分类的缓存清空
        if (content.getCategoryId().longValue() != categoryId.longValue()) {
            redisTemplate.boundHashOps("content").delete(categoryId);
        }

        contentMapper.updateByPrimaryKey(content);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbContent findOne(Long id) {
        return contentMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            //根据主键查询分类ID
            Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();
            //清空分类下的缓存
            redisTemplate.boundHashOps("content").delete(categoryId);
            contentMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbContent content, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbContentExample example = new TbContentExample();
        Criteria criteria = example.createCriteria();

        if (content != null) {
            if (content.getTitle() != null && content.getTitle().length() > 0) {
                criteria.andTitleLike("%" + content.getTitle() + "%");
            }
            if (content.getUrl() != null && content.getUrl().length() > 0) {
                criteria.andUrlLike("%" + content.getUrl() + "%");
            }
            if (content.getPic() != null && content.getPic().length() > 0) {
                criteria.andPicLike("%" + content.getPic() + "%");
            }
            if (content.getStatus() != null && content.getStatus().length() > 0) {
                criteria.andStatusLike("%" + content.getStatus() + "%");
            }
        }

        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据分类ID查询广告列表
     *
     * @param categoryId
     * @return
     */
    public List<TbContent> findByCategoryId(Long categoryId) {
        //1.根据分类ID查询缓存数据
        List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);
        if (CollectionUtils.isEmpty(contentList)) {   //2.如果缓存中没有数据，则查询数据库

            TbContentExample contentExample = new TbContentExample();
            Criteria criteria = contentExample.createCriteria();
            //设置分类ID
            criteria.andCategoryIdEqualTo(categoryId);
            //默认查询有效
            criteria.andStatusEqualTo("1");
            //根据排序查询
            contentExample.setOrderByClause("sort_order");   //默认升序
            contentList = contentMapper.selectByExample(contentExample);

            //将查询到的数据同步到缓存中
            redisTemplate.boundHashOps("content").put(categoryId, contentList);
        } else {
            System.out.println("从缓存中查询数据");
        }

        return contentList;
    }

}
