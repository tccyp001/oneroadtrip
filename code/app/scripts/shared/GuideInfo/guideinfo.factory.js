;(function() {
'use strict';

angular.module('app.shared')
    .factory('GuideInfo', [
        '$http',
        'CityInfo',
        GuideInfoFactory
    ]);


function GuideInfoFactory($http, CityInfo) {

    function GuideInfo() {
        this.guide_id  = '';
        this.name = '';
        this.description = '';
        this.max_people = 0;
        this.has_car = false;
        this.score = 0;
        this.topic = [];
        this.phone = '';
   		this.host_city = CityInfo;
		this.cover_city = CityInfo;       
    }

    return new GuideInfo();
}

}());
