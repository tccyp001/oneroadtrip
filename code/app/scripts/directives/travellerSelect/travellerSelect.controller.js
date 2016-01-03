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

  $scope.$watch('selected', function(val) {
    if (!val) {
      $scope.selected_city_name = undefined;
    }
  })

  $scope.select = function(option){
    $scope.selected_city_name = option.name;
    $scope.selected = option.value;
    $scope.showlayer = false;
  }

  $scope.showlayer = false;

  $scope.show = function(){
    $scope.showlayer = !$scope.showlayer;
  }
}

}());

