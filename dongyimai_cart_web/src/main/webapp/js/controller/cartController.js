app.controller('cartController', function ($scope, cartService) {

    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;
                $scope.totalValue = cartService.sum($scope.cartList);
            })
    }


    //添加购物车
    $scope.addGoodsToCartList = function (itemId, num) {
        cartService.addGoodsToCartList(itemId, num).success(
            function (response) {
                if (response.success) {
                    //刷新购物车列表
                    $scope.findCartList();
                } else {
                    alert(response.message);
                }
            })
    }

    $scope.findListByUserId = function () {
        cartService.findListByUserId().success(
            function (resposne) {
                $scope.addressList = resposne;
                for (var i = 0; i < $scope.addressList.length; i++) {
                    if ($scope.addressList[i].isDefault == '1') {
                        $scope.address = $scope.addressList[i];
                        break;
                    }
                }

            })
    }

    //选择地址
    $scope.selectAddress = function (address) {
        $scope.address = address;
    }
    //判断是否选中地址
    $scope.isSelect = function (address) {
        if ($scope.address == address) {
            return true;
        } else {
            return false;
        }
    }

    $scope.order = {'paymentType': '1'};

    //选择支付方式
    $scope.selectPayType = function (type) {
        $scope.order.paymentType = type;
    }

    //提交订单
    $scope.submitOrder = function () {
        //设置收货地址、收货电话、收货人
        $scope.order.receiverAreaName = $scope.address.address;
        $scope.order.receiverMobile = $scope.address.mobile;
        $scope.order.receiver = $scope.address.contact;

        cartService.submitOrder($scope.order).success(
            function (response) {
                if (response.success) {

                    if ($scope.order.paymentType == '1') {    //判断支付方式为线上支付
                        //跳转到支付页
                        location.href = "pay.html";
                    } else {
                        location.href = "paysuccess.html";
                    }

                } else {
                    alert($scope.order)
                    alert(response.message);
                }
            })
    }

})