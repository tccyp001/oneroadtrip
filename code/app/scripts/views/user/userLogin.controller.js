;(function() {
'use strict';

/* Controllers */

angular.module('app.controllers')
.controller('UserLoginCtrl', [
    '$scope',
    '$http',
    '$location',
    UserLoginCtrl
]);


function UserLoginCtrl($scope, $http, $location) {

	console.log($location.url());

}

}());