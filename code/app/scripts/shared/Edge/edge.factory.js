;(function() {
'use strict';

angular.module('app.shared')
    .factory('Edge', [
        '$http',
        'CityInfo',
        EdgeFactory
    ]);


function EdgeFactory($http, CityInfo) {

    function Edge(opt) {
        this.from_city = CityInfo;
        this.to_city = CityInfo;
        this.distance = '';
        this.hours = '';
        this.transportation_type = 'SHUTTLE';
    }

    return new Edge();
}

}());
