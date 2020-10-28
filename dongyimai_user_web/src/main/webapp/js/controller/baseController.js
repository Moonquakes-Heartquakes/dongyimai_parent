app.controller('baseController', function ($scope) {
    //声明是所有业务模块常用的基础方法

    //设置分页组件的参数
    $scope.paginationConf = {
        'currentPage': 1,	//当前页
        'totalItems': 10,    //总记录数
        'itemsPerPage': 10,   //每页显示记录数
        'perPageOptions': [10, 20, 30, 40, 50],  //每页显示记录选择器
        onChange: function () {
            //执行分页查询
            //$scope.findPage($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
            $scope.reloadList();
        }
    }
    $scope.reloadList = function () {
        //$scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }

    $scope.selectIds = [];  //初始化id数组的数据结构
    //选中/反选
    $scope.updateSelection = function ($event, id) {
        //判断复选框选中还是反选
        if ($event.target.checked) {
            $scope.selectIds.push(id);   //向数组添加元素
        } else {
            var index = $scope.selectIds.indexOf(id);  //返回元素的下标
            $scope.selectIds.splice(index, 1);    //数组移除元素  参数1：数组元素的下标 参数2：移除的个数
        }

    }

    //将JSON结构字符串转换
    $scope.jsonToString = function (jsonString,key){
        //1.类型转换
        var json = JSON.parse(jsonString);
        var value = "";
        for(var i=0;i<json.length;i++){
            if(i>0){
                value+=",";
            }
            //{"id":1,"text":"联想"}
            value += json[i][key];
        }

        return value;
    }





})