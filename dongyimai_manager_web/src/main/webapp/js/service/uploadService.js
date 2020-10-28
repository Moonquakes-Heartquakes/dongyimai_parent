app.service('uploadService',function ($http){
    this.uploadFile = function (){
        //获取表单上的上传控件的数据
        var formData = new FormData();
        formData.append('file',file.files[0]);
        return $http({
            'method':'POST',
            'url':'../upload/uploadFile.do',
            'data': formData,
            'headers':{'Content-Type':undefined},  //修改上传文件时的数据类型，默认使用数据流的方式
            'transformRequest': angular.identity    //angularJS框架对上传数据进行序列化
        });
    }
})