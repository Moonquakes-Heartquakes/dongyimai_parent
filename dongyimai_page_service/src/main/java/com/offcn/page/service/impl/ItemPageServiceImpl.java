package com.offcn.page.service.impl;

//import com.alibaba.dubbo.config.annotation.Service;

import com.offcn.mapper.TbGoodsDescMapper;
import com.offcn.mapper.TbGoodsMapper;
import com.offcn.mapper.TbItemCatMapper;
import com.offcn.mapper.TbItemMapper;
import com.offcn.page.service.ItemPageService;
import com.offcn.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: ysp
 * @Date: 2020/10/12 11:21
 * @Description:
 */
@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Value("${pageDir}")
    private String pageDir;


    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;


    /**
     * 生成商品详情页
     *
     * @param goodsId
     * @return
     */
    public boolean genItemHtml(Long goodsId) {

        try {
            //1.创建模板对象
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            Template template = configuration.getTemplate("item.ftl");
            //2.构建数据源，
            Map dataSource = new HashMap();
            //通过商品ID查询SPU信息
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(goodsId);
            //3.根据商品ID查询扩展表
            TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            //4.根据商品ID查询SKU信息
            TbItemExample tbItemExample = new TbItemExample();
            TbItemExample.Criteria criteria = tbItemExample.createCriteria();
            //设置查询条件
            criteria.andGoodsIdEqualTo(goodsId);    //商品ID
            criteria.andStatusEqualTo("1");    //审核状态  正常
            tbItemExample.setOrderByClause("is_default desc");   //根据默认显示  倒序排序
            //执行查询
            List<TbItem> itemList = itemMapper.selectByExample(tbItemExample);


            //查询商品分类信息
            //一级分类
            TbItemCat itemCat1 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id());
            //二级分类
            TbItemCat itemCat2 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id());
            //三级分类
            TbItemCat itemCat3 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());

            dataSource.put("goods", tbGoods);
            dataSource.put("goodsDesc", tbGoodsDesc);
            dataSource.put("itemCat1", itemCat1);
            dataSource.put("itemCat2", itemCat2);
            dataSource.put("itemCat3", itemCat3);
            dataSource.put("itemList", itemList);
            //5.通过FileWriter对象输出html文件
            FileWriter out = new FileWriter(new File(pageDir + goodsId + ".html"));
            //6.通过模板对象生成静态资源文件
            template.process(dataSource, out);
            //7.关闭文件流
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 批量删除商品详情页
     *
     * @param goodsIds
     * @return
     */
    public boolean deleteItemHtml(Long[] goodsIds) {

        try {
            for (Long id : goodsIds) {
                new File(pageDir + id + ".html").delete();
                System.out.println(id+":静态页面删除成功");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
