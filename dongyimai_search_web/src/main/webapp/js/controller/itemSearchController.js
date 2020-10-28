app.controller('itemSearchController', function ($scope,$location, itemSearchService) {

    $scope.search = function () {
        //对当前页码数据类型做转换
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
        itemSearchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;
                buildPageLabel();   //进行分页展示
            })
    }
    //初始化查询条件对象的数据结构
    $scope.searchMap = {
        'keywords': '',
        'category': '',
        'brand': '',
        'spec': {},
        'price': '',
        'pageNo': 1,
        'pageSize': 20,
        'sortValue':'',
        'sortField':''
    };

    //添加查询条件
    $scope.addSearchItem = function (key, value) {
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = value;
        } else {
            $scope.searchMap.spec[key] = value;
        }
        $scope.searchMap.pageNo = 1;
        //执行查询
        $scope.search();
    }

    //撤销查询条件
    $scope.removeSearchItem = function (key) {
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = '';
        } else {
            //移除对象
            delete $scope.searchMap.spec[key];
        }
        $scope.searchMap.pageNo = 1;
        //执行查询
        $scope.search();
    }

    //构建页码
    buildPageLabel = function () {
        $scope.pageLabel = [];
        var firstPage = 1;   //数组中的起始页码
        var lastPage = $scope.resultMap.totalPages;//数组中的结束页码
        var maxPage = $scope.resultMap.totalPages;
        $scope.firstDot = true;
        $scope.lastDot = true;

        if (maxPage > 5) {
            if ($scope.searchMap.pageNo <=3) {
                lastPage = 5;               //当当前页小于3页时，将结束页码固定到第5页
                $scope.firstDot = false;   //左侧省略号不显示
            } else if ($scope.searchMap.pageNo >= maxPage - 2) {
                firstPage = maxPage - 4;    //当 当前页大于总页数-2时，将起始页码固定
                $scope.lastDot = false;     //右侧省略号不显示
            } else {
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
            }
        }else{
            $scope.firstDot = false;
            $scope.lastDot = false;
        }

        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i);
        }
    }

    //提交当前页码查询
    $scope.queryByPage = function (pageNo) {
        //对当前页码做格式验证
        if (pageNo < 1 || pageNo > $scope.resultMap.totalPages) {
            return;
        }
        $scope.searchMap.pageNo = pageNo;
        //执行查询
        $scope.search();
    }
    //判断当前页是否是首页
    $scope.isTopPage = function (){
        if($scope.searchMap.pageNo==1){
            return true;
        }else{
            return false;
        }
    }

    //判断当前页是否是末页
    $scope.resultMap = {'totalPages':1};
    $scope.isEndPage = function (){
        if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        }else{
            return false;
        }
    }
    //判断指定页码是否是当前页
    $scope.isPage = function (pageNo){
        if($scope.searchMap.pageNo==pageNo){
            return true;
        }else{
            return false;
        }
    }

    //排序查询
    $scope.sortSearch =function (sortField,sortValue){
        $scope.searchMap.sortField = sortField;   //排序字段
        $scope.searchMap.sortValue = sortValue;  //排序规则
        //执行查询
        $scope.search();
    }

    //判断关键字是否是品牌
    $scope.keywordsIsBrand = function (){
        //遍历品牌列表
        for(var i = 0;i<$scope.resultMap.brandList.length;i++){
            if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>-1){
                return true;
            }
        }
        return false;
    }

    //加载首页查询关键字
    $scope.loadKeywords = function (){
       $scope.searchMap.keywords =  $location.search()['keywords'];
       //执行查询
        $scope.search();
    }


})