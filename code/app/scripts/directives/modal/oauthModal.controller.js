;(function() {
'use strict';

/* Controllers */

angular.module('app.controllers')
.controller('OauthModalCtrl', [
    '$scope',
    '$modalInstance',
    '$http',
    '$cookies',
    '$cookieStore',
    '$location',
    OauthModalCtrl
]);


function OauthModalCtrl($scope, $modalInstance, $http, $cookies, $cookieStore, $location) {
  console.log($location.url);

}

}());


