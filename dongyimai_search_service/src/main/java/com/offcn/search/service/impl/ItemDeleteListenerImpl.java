package com.offcn.search.service.impl;

import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;

/**
 * @Auther: ysp
 * @Date: 2020/10/13 14:15
 * @Description:
 */
@Component
public class ItemDeleteListenerImpl implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    public void onMessage(Message message) {
        if (message instanceof ObjectMessage) {
            ObjectMessage objectMessage = (ObjectMessage) message;
            //转换成ID数组形式
            try {
                Long[] ids = (Long[]) objectMessage.getObject();
                itemSearchService.deleteByGoodsIds(Arrays.asList(ids));

            } catch (JMSException e) {
                e.printStackTrace();
            }

        }
    }
}
