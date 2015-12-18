;(function() {
'use strict';

/* Controllers */

angular.module('app.controllers')
.controller('TravellerSelectCtrl', [
    '$scope',
    '$http',
    TravellerSelectCtrl
]);


function TravellerSelectCtrl($scope, $http) {

  $scope.selected = undefined;

  $scope.select = function(option){
    $scope.selected = option.name;
    $scope.showlayer = false;
  }

  $scope.showlayer = false;

  $scope.show = function(){

    $scope.showlayer = !$scope.showlayer;
  }
}

}());

