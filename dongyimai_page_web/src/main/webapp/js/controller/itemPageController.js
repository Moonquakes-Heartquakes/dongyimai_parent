app.controller('itemPageController', function ($scope,$http) {

    //点击+ -  对购买数量进行赋值
    $scope.addNum = function (num) {
        $scope.num = $scope.num + num;

        //格式判断
        if ($scope.num < 1) {
            $scope.num = 1;
        }
    }


    $scope.specificationItem = {};  //初始化规格对象的数据结构
    //选择规格  name 规格名称   value 规格选项
    $scope.selectSpecification = function (name, value) {
        $scope.specificationItem[name] = value;
        searchSku();
    }
    //判断规格选项是否是选中的规格选项
    $scope.isSelect = function (name, value) {
        if ($scope.specificationItem[name] == value) {
            return true;
        } else {
            return false;
        }
    }


    //加载SKU的信息
    $scope.loadSku = function () {
        $scope.sku = skuList[0];
        //选中规格
        $scope.specificationItem = JSON.parse(JSON.stringify($scope.sku.spec));     //深克隆
    }

    //匹配对象是否一致
    matchObject = function (map1,map2){
        for(key in map1){
            if(map1[key]!=map2[key]){
                return false;
            }
        }

        for(key in map2){
            if(map2[key]!=map1[key]){
                return false;
            }
        }
        return true;
    }

    searchSku = function (){
        for(var i=0;i<skuList.length;i++){
            //匹配规格是否一致
            if(matchObject(skuList[i].spec,$scope.specificationItem)){
                //如果一致，则选中该SKU对象
                $scope.sku = skuList[i];
                return;
            }
        }
        //没有匹配到SKU对象
        $scope.sku = {"id":0,"title":"-----","price":0};
    }

    //添加购物车
    $scope.addCart = function (){
        // alert('---'+$scope.sku.id);
        //location.href="http://localhost:9108/cart/addGoodsToCartList.do?itemId="+$scope.sku.id+"&num="+$scope.num;
        $http.get('http://localhost:9108/cart/addGoodsToCartList.do?itemId='+$scope.sku.id+'&num='+$scope.num,{'withCredentials':true}).success(
            function(response){
                if(response.success){
                    location.href='http://localhost:9108/cart.html';
                }else{
                    alert(response.message);
                }
            })

    }

})