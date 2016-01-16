;(function() {
'use strict';

angular.module('app.shared')
    .factory('CitiInfo', [
        '$http',
        CitiInfoFactory
    ]);


function CitiInfoFactory($http) {

    function CitiInfo(opt) {
        this.city_id  = '';
        this.name = '';
        this.cn_name = '';
        this.suggest = 0;
        this.min = 0;
        this.alias = [];

    }

    return new CitiInfo();
}

}());
