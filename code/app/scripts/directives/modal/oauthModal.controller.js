;(function() {
'use strict';

/* Controllers */

angular.module('app.controllers')
.controller('OauthModalCtrl', [
    '$scope',
    '$timeout',
    '$http',
    '$window',
    '$cookieStore',
    '$location',
    'AccessToken',
    OauthModalCtrl
]);


function OauthModalCtrl($scope, $timeout, $http, $window, $cookieStore, $location, AccessToken) {
    var hash = $location.path().substr(1);

    AccessToken.setTokenFromString(hash);

    $scope.$watchCollection(function(){
        return AccessToken;
    }, function(val) {
        if (val.status === 'done') {
          $timeout(function(){
              $window.close();
          }, 3000);
        }
    })

} 

}());


