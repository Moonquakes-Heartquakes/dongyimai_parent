app.service('payService', function ($http) {
    //预下单，得到支付二维码
    this.createNative = function () {
        return $http.get('../pay/createNative.do');
    }

    //查询交易状态
    this.queryPayStatus = function (out_trade_no){
        return $http.get('../pay/queryPayStatus.do?out_trade_no='+out_trade_no);
    }
})