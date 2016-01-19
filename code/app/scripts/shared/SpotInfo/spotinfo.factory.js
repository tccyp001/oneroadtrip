;(function() {
'use strict';

angular.module('app.shared')
    .factory('SpotInfo', [
        '$http',
        SpotInfoFactory
    ]);


function SpotInfoFactory($http) {
    function SpotInfo(opt) {
        this.spot_id = '';
        this.city_id  = '';
        this.name = '';
        this.cn_name = '';
        this.suggest = 0;
        this.description = '';
        this.hours = 0;
        this.score = 0;
        this.topics = '';
    }

    return new SpotInfo();
}

}());
