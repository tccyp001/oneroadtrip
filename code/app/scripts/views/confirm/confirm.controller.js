;(function() {
'use strict';

/* Controllers */

angular.module('app.controllers')
.controller('ConfirmCtrl', [
    '$scope',
    '$http',
    '$modal',
    'Controller',
    'TourInfo',
    ConfirmCtrl
]);


function ConfirmCtrl($scope, $http, $modal, Controller, TourInfo) {
	$scope.$parent.showfooter = true;
	
	// $scope.order_id = TourInfo.itinerary.order.order_id;
	$scope.order_id = '11111111';
}

}());