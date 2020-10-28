package com.offcn.page.service.impl;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * @Auther: ysp
 * @Date: 2020/10/13 15:03
 * @Description:
 */
@Component
public class ItemPageDeleteListenerImpl implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    public void onMessage(Message message) {
        if (message instanceof ObjectMessage) {
            ObjectMessage objectMessage = (ObjectMessage) message;
            try {
                Long[] goodsIds = (Long[]) objectMessage.getObject();
                itemPageService.deleteItemHtml(goodsIds);

            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
