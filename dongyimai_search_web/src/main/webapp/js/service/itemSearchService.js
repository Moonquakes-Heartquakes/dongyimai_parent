app.service('itemSearchService', function ($http) {

    this.search = function (searchMap) {
        return $http.post('../itemsearch/search.do', searchMap);
    }
})