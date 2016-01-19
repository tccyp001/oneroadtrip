;(function() {
'use strict';

angular.module('app.shared')
    .factory('CitiInfo', [
        '$http',
        'Controller',
        CitiInfoFactory
    ]);


function CitiInfoFactory($http, Controller) {

    function CitiInfo() {
        // this.city_id  = '';
        // this.name = '';
        // this.cn_name = '';
        // this.suggest = 0;
        // this.min = 0;
        // this.alias = [];
        this.children = [];
    }

    CitiInfo.prototype.getList = function(){
        var that = this;
        $http.get(Controller.base() + 'api/city').then(function(res){
            // $scope.options.city = _.map(res.data.city, function(city){
            //     return {
            //         name: city.cn_name,
            //         value: city.city_id,
            //         min: city.min,
            //         alias: city.alias
            //     }
            // })
            that.children = res.data.city;
        });
    }

    return new CitiInfo();
}

}());
