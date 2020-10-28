package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Result;
import com.offcn.order.service.OrderService;
import com.offcn.pay.service.AliPayService;
import com.offcn.pojo.TbPayLog;
import com.offcn.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: ysp
 * @Date: 2020/10/19 14:17
 * @Description:
 */
@RestController
@RequestMapping("/pay")
public class AliPayController {

    @Reference
    private AliPayService aliPayService;

    @Reference
    private OrderService orderService;

    @Autowired
    private IdWorker idWorker;


    @RequestMapping("/createNative")
    public Map createNative() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        //从缓存中查询支付日志，得到交易订单编号，和支付总金额
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);

        if (payLog != null) {
            return aliPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee() + "");
        } else {
            return new HashMap();
        }
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        Result result = null;

        int i = 0;
        while (true) {

            Map map = null;

            try {
                map = aliPayService.queryPayStatus(out_trade_no);
            } catch (Exception e) {
                //e.printStackTrace();
                System.out.println("调用查询接口失败");
            }
            if (map == null) {  //调用接口出错
                result = new Result(false, "支付出错");
                break;
            }

            /*交易状态：WAIT_BUYER_PAY（交易创建，等待买家付款）、
            TRADE_CLOSED（未付款交易超时关闭，或支付完成后全额退款）、
            TRADE_SUCCESS（交易支付成功）、
            TRADE_FINISHED（交易结束，不可退款）*/
            if (((String) map.get("status")).equals("TRADE_SUCCESS")) {
                result = new Result(true, "交易支付成功");

                //修改支付状态
                orderService.updateOrderStatus(out_trade_no,(String)map.get("tradeNo"));
                break;
            }
            if (((String) map.get("status")).equals("TRADE_CLOSED")) {
                result = new Result(true, "未付款交易超时关闭，或支付完成后全额退款");
                break;
            }
            if (((String) map.get("status")).equals("TRADE_FINISHED")) {
                result = new Result(true, "交易结束，不可退款");
                break;
            }

            i++;
            if (i >= 10) {   //设置超时时间
                result = new Result(false, "二维码超时");
                break;
            }
            try {
                Thread.sleep(3000);   //每3S执行一次循环
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        return result;
    }
}
