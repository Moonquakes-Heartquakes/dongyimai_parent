package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Goods;
import com.offcn.mapper.*;
import com.offcn.pojo.*;
import com.offcn.pojo.TbGoodsExample.Criteria;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional   //声明注解式事务
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbBrandMapper brandMapper;

    @Autowired
    private TbSellerMapper sellerMapper;

    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     *
     * @param goods
     */
    public void add(Goods goods) {
        //1.设置商品审核状态 为未审核
        goods.getGoods().setAuditStatus("0");
        //2.保存商品信息
        goodsMapper.insert(goods.getGoods());
        //3.获得商品主键ID   mapper.xml
        //4.在商品扩展信息中设置ID
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        //5.保存商品扩展信息
        goodsDescMapper.insert(goods.getGoodsDesc());

        //int i = 10/0;

        //6.保存商品详情信息SKU
        //判断是否启用规格
        this.saveItem(goods);
    }


    private void setItemList(TbItem item, Goods goods) {
        item.setCategoryid(goods.getGoods().getCategory3Id());        //商品分类ID ，使用三级分类ID
        item.setCreateTime(new Date());                              //创建时间
        item.setUpdateTime(new Date());                              //更新时间
        item.setGoodsId(goods.getGoods().getId());                   //商品ID
        item.setSellerId(goods.getGoods().getSellerId());            //商家ID

        //查询分类名称
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(item.getCategoryid());
        item.setCategory(itemCat.getName());                        //分类名称
        //查询品牌名称
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        item.setBrand(brand.getName());                             //品牌名称
        //查询商家名称
        TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        item.setSeller(seller.getNickName());                       //店铺名称

        List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if (!CollectionUtils.isEmpty(imageList)) {
            item.setImage((String) imageList.get(0).get("url"));    //商品图片
        }

        //执行保存SKU信息
        itemMapper.insert(item);
    }


    private void saveItem(Goods goods) {
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            if (!CollectionUtils.isEmpty(goods.getItemList())) {
                for (TbItem item : goods.getItemList()) {
                    String title = goods.getGoods().getGoodsName();   //SPU名称
                    //拼接SKU名称  SPU名称+规格选项
                    Map<String, String> spec = JSON.parseObject(item.getSpec(), Map.class);
                    for (String key : spec.keySet()) {
                        title += " " + spec.get(key);
                    }
                    item.setTitle(title);                                        //SKU名称
                    this.setItemList(item, goods);
                }
            }
        } else {
            TbItem item = new TbItem();
            item.setTitle(goods.getGoods().getGoodsName());     //商品名称
            item.setPrice(goods.getGoods().getPrice());        //商品价格
            item.setNum(9999);                                  //库存
            item.setStatus("1");                                //是否启用
            item.setIsDefault("1");                             //是否默认
            item.setSpec("{}");                                 //规格为空
            this.setItemList(item, goods);

        }
    }

    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {
        //1.重置审核状态  未审核
        goods.getGoods().setAuditStatus("0");
        //2.修改SPU信息
        goodsMapper.updateByPrimaryKey(goods.getGoods());
        //3.修改扩展信息
        goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
        //4.根据商品ID批量删除SKU信息
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andGoodsIdEqualTo(goods.getGoods().getId());
        //执行删除
        itemMapper.deleteByExample(tbItemExample);
        //5.重新添加SKU信息
        this.saveItem(goods);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        //1.根据ID查询商品SPU信息
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        //2.根据ID查询扩展信息
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        //3.根据商品ID查询SKU信息集合
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<TbItem> itemList = itemMapper.selectByExample(tbItemExample);
        //4.设置复合实体
        Goods goods = new Goods();
        goods.setGoods(tbGoods);
        goods.setGoodsDesc(tbGoodsDesc);
        goods.setItemList(itemList);
        return goods;

    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            //goodsMapper.deleteByPrimaryKey(id);    物理删除
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setIsDelete("1");   //逻辑删除
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
        List<TbItem> itemList = this.findItemListByGoodsIdAndStatus(ids,"1");
        //将SKU信息设置为禁用
        for(TbItem item:itemList){
            item.setStatus("0");
            itemMapper.updateByPrimaryKey(item);
        }


    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();

        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                //criteria.andSellerIdLike("%" + goods.getSellerId() + "%");
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            /*if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }*/
            criteria.andIsDeleteIsNull();  //表示未删除的数据
        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量审核商品
     *
     * @param ids    商品ID集合
     * @param status 状态
     */
    public void updateStatus(Long[] ids, String status) {
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setAuditStatus(status);
            goodsMapper.updateByPrimaryKey(tbGoods);
            //更新关联SKU信息的状态
            TbItemExample itemExample = new TbItemExample();
            TbItemExample.Criteria criteria = itemExample.createCriteria();
            //设置商品ID
            criteria.andGoodsIdEqualTo(tbGoods.getId());
            //执行查询SKU集合
            List<TbItem> itemList = itemMapper.selectByExample(itemExample);
            if (!CollectionUtils.isEmpty(itemList)) {
                for (TbItem tbItem : itemList) {
                    tbItem.setStatus(status);
                    //修改SKU的状态
                    itemMapper.updateByPrimaryKey(tbItem);
                }
            }
        }
    }

    /**
     * 根据商品ID和状态查询SKU列表
     *
     * @param ids
     * @param status
     * @return
     */
    public List<TbItem> findItemListByGoodsIdAndStatus(Long[] ids, String status) {
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        //设置查询条件
        criteria.andGoodsIdIn(Arrays.asList(ids));
        criteria.andStatusEqualTo(status);

        return itemMapper.selectByExample(tbItemExample);
    }

}
