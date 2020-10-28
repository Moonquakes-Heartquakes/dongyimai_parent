package com.offcn.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

/**
 * @Auther: ysp
 * @Date: 2020/10/13 13:58
 * @Description:
 */
@Component
public class ItemSearchListenerImpl implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;


    public void onMessage(Message message) {
        //类型强制转换
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            try {
                List<TbItem> itemList = JSON.parseArray(textMessage.getText(), TbItem.class);  //JSON结构的字符串
                itemSearchService.importItem(itemList);

            } catch (JMSException e) {
                e.printStackTrace();
            }


        }
    }
}
