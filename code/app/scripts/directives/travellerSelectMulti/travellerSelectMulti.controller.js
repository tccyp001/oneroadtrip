;(function() {
'use strict';

/* Controllers */

angular.module('app.controllers')
.controller('travellerSelectMultiCtrl', [
    '$scope',
    '$http',
    travellerSelectMultiCtrl
]);


function travellerSelectMultiCtrl($scope, $http) {


  $scope.$watch('options', function(val){
    $scope.options_copy = _.clone(val);
  })

  $scope.$watchCollection('selected_city', function(val){
    if (val && val.length === 0) {
      $scope.selected_show = true;
    }
  })

  $scope.selected_city_ids = [];
  $scope.selected_city = [];
  $scope.selected_show = true;

  $scope.select = function(option){
    $scope.selected_show = false;
    $scope.selected_city_ids.push({"city_id" : option.city_id});
    $scope.selected_city.push(option);
    $scope.selected = $scope.selected_city_ids;
    var index = $scope.options_copy.indexOf(option);
    $scope.options_copy.splice(index, 1);
    $scope.showlayer = false;
  }


  $scope.removeSelectedCity = function(city){
    var index = $scope.selected_city.indexOf(city);
    $scope.selected_city.splice(index, 1);
    $scope.options_copy.push(city);
  }

  $scope.showlayer = false;

  $scope.show = function(){

    $scope.showlayer = !$scope.showlayer;
  }
}

}());

