var app = angular.module('dongyimai', []);  //参数2，引入第三方组件
//angularJs的过滤器
app.filter('trustHtml',['$sce',function ($sce){
    return function (data){
        return $sce.trustAsHtml(data);
    }
}])