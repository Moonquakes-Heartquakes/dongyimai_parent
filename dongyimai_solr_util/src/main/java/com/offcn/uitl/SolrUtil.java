package com.offcn.uitl;

import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: ysp
 * @Date: 2020/9/30 15:17
 * @Description: 批量导入Solr的工具类
 */
@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    public void importItem() {
        //查询SKU集合  审核通过
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        //设置状态为 审核通过
        criteria.andStatusEqualTo("1");
        List<TbItem> itemList = itemMapper.selectByExample(tbItemExample);

        for (TbItem item : itemList) {
            Map<String, String> specMap = JSON.parseObject(item.getSpec(), Map.class);

            Map<String, String> pinyinMap = new HashMap<String, String>();
            for (String key : specMap.keySet()) {
                //将key做拼音转换
                pinyinMap.put(Pinyin.toPinyin(key, "").toLowerCase(), specMap.get(key));
            }

            item.setSpecMap(pinyinMap);

            System.out.println(item.getTitle() + "---" + item.getPrice());
        }

        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
        System.out.println("导入成功");

    }

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext-*.xml");
        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");

        solrUtil.importItem();

    }
}
