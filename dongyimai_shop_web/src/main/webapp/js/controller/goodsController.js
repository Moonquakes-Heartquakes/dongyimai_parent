//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService, uploadService, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function () {
        //接收ID
        var id = $location.search()['id'];
        if (null == id) {
            return;
        }
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                //设置富文本编辑器的值
                editor.html($scope.entity.goodsDesc.introduction);
                //商品图片数据类型转换
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                //扩展属性数据类型转换
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                //规格数据类型转换
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);

                //SKU数据中的规格选项类型转换
                for (var i = 0; i < $scope.entity.itemList.length; i++) {
                    $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
                }
            }
        );
    }


    //设置规格选项是否选中
    $scope.checkAttributeValue = function (specName, optionName) {
        //1.使用扩展信息表中的规格集合
        var items = $scope.entity.goodsDesc.specificationItems;
        var object = $scope.searchObjectByKey(items, 'attributeName', specName);
        if (object == null) {
            return false;
        } else {
            if (object.attributeValue.indexOf(optionName) > -1) {
                return true;
            } else {
                return false;
            }
        }

    }


    //保存
    $scope.save = function () {
        //富文本编辑器
        $scope.entity.goodsDesc.introduction = editor.html();
        var serviceObject;//服务层对象
        if ($scope.entity.goods.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    //$scope.reloadList();//重新加载
                    location.href="goods.html";
                } else {
                    alert(response.message);
                }
            }
        );
    }


   /* $scope.add = function () {
        //设置富文本编辑器的内容
        $scope.entity.goodsDesc.introduction = editor.html();
        goodsService.add($scope.entity).success(
            function (response) {
                if (response.success) {
                    //清空表单
                    //$scope.entity = {};

                    $scope.entity = {'itemImages': [], 'specificationItems': []};
                    //清空富文本编辑器
                    editor.html('');
                } else {
                    alert(response.message);
                }
            })
    }*/


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }


    $scope.item_image_entity = {};//初始化上传图片表单对象的数据结构
    //上传文件
    $scope.upload = function () {
        uploadService.uploadFile().success(
            function (response) {
                if (response.success) {
                    $scope.item_image_entity.url = response.message;
                } else {
                    alert(response.message);
                }
            }).error(function () {
            alert("上传发生错误");
        })
    }

    $scope.entity = {'goods': {}, 'goodsDesc': {'itemImages': [], 'specificationItems': []}}//初始化商品信息的数据结构
    $scope.add_image_entity = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.item_image_entity);
    }

    $scope.delete_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    }

    //查询一级分类列表
    $scope.selectItemCat1List = function () {
        itemCatService.findByParentId(0).success(
            function (response) {
                $scope.itemCat1List = response;
            })
    }


    //联动查询二级分类   //watch  监控数据变化，如果数据发生变化则执行对应的方法
    $scope.$watch('entity.goods.category1Id', function (newValue, oldValue) {
        //如果newValue有值，则说明数据发生变化
        if (newValue) {
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat2List = response;
                })
        }
    })
    //联动查询三级分类
    $scope.$watch('entity.goods.category2Id', function (newValue, oldValue) {
        if (newValue) {
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat3List = response;
                })
        }
    })
    //联动查询分类对象，查询模板ID
    $scope.$watch('entity.goods.category3Id', function (newValue, oldValue) {
        if (newValue) {
            itemCatService.findOne(newValue).success(
                function (response) {
                    $scope.entity.goods.typeTemplateId = response.typeId;   //模板ID
                })
        }
    })
    //根据模板ID查询模板对象，联动查询品牌列表
    $scope.$watch('entity.goods.typeTemplateId', function (newValue, oldValue) {
        if (newValue) {
            typeTemplateService.findOne(newValue).success(
                function (response) {
                    $scope.typeTemplate = response;
                    $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);     //品牌类型转换
                    if ($location.search()['id'] == null) {
                        $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);    //扩展属性类型转换
                    }
                })

            //联动查询规格列表
            typeTemplateService.findSpecList(newValue).success(
                function (response) {
                    $scope.specList = response;
                })
        }
    })

    //name  规格名称的值    value   规格选项的值
    $scope.updateSpecSelection = function ($event, name, value) {
        //1.判断specificationItems集合中是否有选中的选项
        var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, 'attributeName', name);
        // 2.如果没有该选项，则specificationItems   push
        if (object == null) {
            $scope.entity.goodsDesc.specificationItems.push({"attributeName": name, "attributeValue": [value]});
        } else {
            // 3.如果有该选项，则判断是否选中还是反选   如果选中操作 则  attributeValue  push
            if ($event.target.checked) {
                object.attributeValue.push(value);
            } else {    // 	如果反选操作	则  attributeValue  splice
                object.attributeValue.splice(object.attributeValue.indexOf(value), 1);
                // 		判断 attributeValue的length是否为0
                if (object.attributeValue.length == 0) {
                    // 如果为 0  则 specificationItems  splice
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
                }
            }
        }
    }


    //构建SKU的数据结构
    $scope.createItemList = function () {
        //1.初始化SKU集合的数据结构
        $scope.entity.itemList = [{'price': 0, 'num': 9999, 'status': '0', 'isDefault': '0', 'spec': {}}];
        //2.使用扩展信息表中的规格集合
        var items = $scope.entity.goodsDesc.specificationItems;
        //3.遍历规格集合
        for (var i = 0; i < items.length; i++) {
            //4.向SKU的记录中添加列
            $scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
        }
    }

    //columnName 规格名称  columnValues 规格选项集合
    addColumn = function (itemList, columnName, columnValues) {
        var newList = [];   //定义新的集合，用来存放最终拼接之后的元素
        for (var i = 0; i < itemList.length; i++) {
            var oldRow = itemList[i];     //表示原有行记录
            for (var j = 0; j < columnValues.length; j++) {
                var newRow = JSON.parse(JSON.stringify(oldRow));   //深克隆
                newRow.spec[columnName] = columnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
    }

    //审核状态  0.未审核 1.审核通过 2.驳回 3.关闭
    $scope.status = ['未审核', '审核通过', '驳回', '关闭'];

    $scope.catList = [];
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {
                    $scope.catList[response[i].id] = response[i].name;
                }
            })
    }


});