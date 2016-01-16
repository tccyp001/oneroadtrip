;(function() {
'use strict';

angular.module('app.shared')
    .factory('VisitCity', [
        '$http',
        'CityInfo',
        'GuidePlanErrorInfo',
        'GuideInfo',
        VisitCityFactory
    ]);


function VisitCityFactory($http, CityInfo, GuidePlanErrorInfo, GuideInfo) {

    function VisitCity() {
    	this.city = CityInfo;
    	this.num_days = 0;
    	this.suggest_rate = 0;
    	this.start_date = 0;
    	this.error_info = GuidePlanErrorInfo;
    	this.guide = GuideInfo;
    	this.excluded_guide_id = 0;
    }

    return new QuoteInfo();
}

}());
