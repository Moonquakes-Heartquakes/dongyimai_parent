app.service('loginService',function ($http){
    //获得登录信息
    this.getLoginName = function (){
        return $http.get('../login/getLoginName.do');
    }
})