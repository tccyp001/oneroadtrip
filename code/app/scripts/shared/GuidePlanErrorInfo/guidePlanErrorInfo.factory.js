;(function() {
'use strict';

angular.module('app.shared')
    .factory('GuidePlanErrorInfo', [
        '$http',
        'CityInfo',
        GuidePlanErrorInfoFactory
    ]);


function GuidePlanErrorInfoFactory($http, CityInfo) {

    function GuidePlanErrorInfo() {
        this.NOT_FOUND = 1;
    }

    return new GuidePlanErrorInfo();
}

}());

