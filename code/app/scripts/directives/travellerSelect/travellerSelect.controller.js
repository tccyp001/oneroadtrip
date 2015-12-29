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
    $scope.selected_city_name = option.name;
    $scope.selected = option.city_id;
    $scope.showlayer = false;
  }

  $scope.showlayer = false;

  $scope.show = function(){

    $scope.showlayer = !$scope.showlayer;
  }
}

}());

