;(function() {
'use strict';

/* Controllers */

angular.module('app.controllers')
.controller('ResetPwdCtrl', [
    '$scope',
    '$http',
    '$modal',
    'Controller',
    'TourInfo',
    ResetPwdCtrl
]);


function ResetPwdCtrl($scope, $http, $modal, Controller, TourInfo) {
	$scope.$parent.showfooter = true;
}

}());