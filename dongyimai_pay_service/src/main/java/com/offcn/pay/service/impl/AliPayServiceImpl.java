package com.offcn.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.offcn.pay.service.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: ysp
 * @Date: 2020/10/19 14:04
 * @Description:
 */
@Service
public class AliPayServiceImpl implements AliPayService {

    @Autowired
    private AlipayClient alipayClient;

    /**
     * 预下单，返回支付二维码
     *
     * @param out_trade_no 商品订单编号
     * @param total_fee    支付金额（分）
     * @return
     */
    public Map createNative(String out_trade_no, String total_fee) {

        Map result = new HashMap();
        //分转元
        long total = Long.parseLong(total_fee);
        BigDecimal bigTotal = new BigDecimal(total);
        BigDecimal div = new BigDecimal(100L);
        BigDecimal money = bigTotal.divide(div);

        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest(); //创建API对应的request类
        request.setBizContent("{" +
                "\"out_trade_no\":\"" + out_trade_no + "\"," + //商户订单号
                "\"total_amount\":\"" + money.doubleValue() + "\"," +
                "\"subject\":\"测试商品01\"," +
                "\"store_id\":\"NJ_001\"," +
                "\"timeout_express\":\"90m\"}"); //订单允许的最晚付款时间
        AlipayTradePrecreateResponse response = null;
        try {
            response = alipayClient.execute(request);
            System.out.print(response.getBody());
            //判断响应码
            if (response.getCode().equals("10000")) {
                result.put("out_trade_no", response.getOutTradeNo());    //商户订单号
                result.put("qrCode", response.getQrCode());          //二维码链接
                result.put("total_fee", total_fee);
            } else {
                System.out.println("调用接口失败");
            }


        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 查询交易状态
     *
     * @param out_trade_no 商户的订单号
     * @return
     */
    public Map queryPayStatus(String out_trade_no) {
        Map result = new HashMap();
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest(); //创建API对应的request类
        request.setBizContent("{" +
                "    \"out_trade_no\":\""+out_trade_no+"\"," +
                "    \"trade_no\":\"\"}");  //设置业务参数
        AlipayTradeQueryResponse response = null; //通过alipayClient调用API，获得对应的response类
        try {
            response = alipayClient.execute(request);
            System.out.print(response.getBody());
            if(response.getCode().equals("10000")){
                result.put("outTradeNo",response.getOutTradeNo());
                result.put("tradeNo",response.getTradeNo());    //交易流水号
                result.put("status",response.getTradeStatus());    //交易状态
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return result;
    }
}
