package com.offcn.service;

import com.offcn.utils.SmsUtil;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * @Auther: ysp
 * @Date: 2020/10/14 10:35
 * @Description:
 */
@Component
public class SmsServiceListenerImpl implements MessageListener {

    @Autowired
    private SmsUtil smsUtil;

    public void onMessage(Message message) {
        if (message instanceof MapMessage) {
            MapMessage mapMessage = (MapMessage) message;
            try {
                String mobile = mapMessage.getString("mobile");     //手机号
                String code = mapMessage.getString("code");         //短信内容
                HttpResponse response = smsUtil.sendSms(mobile, code);
                System.out.println(EntityUtils.toString(response.getEntity()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
