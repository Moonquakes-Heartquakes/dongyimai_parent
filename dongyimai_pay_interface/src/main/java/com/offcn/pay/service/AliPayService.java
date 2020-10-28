package com.offcn.pay.service;

import java.util.Map;

/**
 * @Auther: ysp
 * @Date: 2020/10/19 13:59
 * @Description:
 */
public interface AliPayService {


    /**
     * 预下单，返回支付二维码
     * @param out_trade_no   商品订单编号
     * @param total_fee   支付金额（分）
     * @return
     */
    public Map createNative(String out_trade_no, String total_fee);


    /**
     * 查询交易状态
     * @param out_trade_no   商户的订单号
     * @return
     */
    public Map queryPayStatus(String out_trade_no);
}
