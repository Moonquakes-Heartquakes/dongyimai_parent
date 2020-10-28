package com.offcn.page.service.impl;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 *
 * @Date: 2020/10/13 14:35
 * @Description:
 */
@Component
public class ItemPageListenerImpl implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    public void onMessage(Message message) {
        if(message instanceof TextMessage){
            TextMessage textMessage = (TextMessage)message;
            try {
                String goodsId = textMessage.getText();
                itemPageService.genItemHtml(Long.parseLong(goodsId));
                System.out.println(goodsId+"：页面生成成功");
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
