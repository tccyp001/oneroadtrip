;(function() {
'use strict';

angular.module('app.shared')
    .factory('TourInfo', [
    '$resource',
    '$http',
    TourInfoFactory
]);


function TourInfoFactory($resource, $http) {
	function Tour(){
		this.data = {};
	}

	return new Tour();

}

}());