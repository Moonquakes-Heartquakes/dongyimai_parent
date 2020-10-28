app.controller('payController', function ($scope, $location, payService) {

    $scope.createNative = function () {
        payService.createNative().success(
            function (response) {
                $scope.outTradeNo = response.out_trade_no;
                $scope.totalFee = (response.total_fee / 100).toFixed(2);    //分  转换成元
                //生成二维码
                var qr = new QRious({
                    'element': document.getElementById("erweima"),
                    'level': 'H',
                    'size': 250,
                    'value': response.qrCode
                });

                queryPayStatus(response.out_trade_no);   //查询支付状态
            })
    }

    queryPayStatus = function (outTradeNo) {
        payService.queryPayStatus(outTradeNo).success(
            function (response) {
                if (response.success) {
                    location.href = "paysuccess.html#?money=" + $scope.totalFee;
                } else {
                    if (response.message == '二维码超时') {
                        document.getElementById("timeout").innerHTML = "二维码已过期，刷新页面重新获取二维码。";
                    } else {
                        location.href = "payfail.html";
                    }
                }
            })
    }

    $scope.getMoney = function () {
        return $location.search()['money'];
    }


})