app.controller('indexController',function ($scope,contentService){


    $scope.categoryList = [];
    $scope.findByCategoryId = function (categoryId){
        contentService.findByCategoryId(categoryId).success(
            function (response){
               // $scope.list = response;
                $scope.categoryList[categoryId] = response;
        })
    }


    //搜索关键字
    $scope.search = function (){
        location.href = "http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }

})