app.service('cartService',function ($http){

    this.findCartList = function (){
        return $http.get('../cart/findCartList.do');
    }

    this.addGoodsToCartList = function (itemId,num){
        return $http.get('../cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
    }


    this.sum = function (cartList){
        var totalValue = {'totalNum':0,'totalMoney':0.00};   //总金额实体
        //遍历购物车
        for(var i=0;i<cartList.length;i++){
            var orderItemList = cartList[i].orderItemList;
            //遍历订单详情对象
            for(var j=0;j<orderItemList.length;j++){
                var orderItem = orderItemList[j];
                totalValue.totalNum += orderItem.num;
                totalValue.totalMoney += orderItem.totalFee;
            }
        }
        return totalValue;
    }

    //查询地址列表
    this.findListByUserId = function (){
        return $http.get('../address/findListByUserId.do');
    }

    //提交订单
    this.submitOrder = function (order){
        return $http.post('../order/add.do',order);
    }
})