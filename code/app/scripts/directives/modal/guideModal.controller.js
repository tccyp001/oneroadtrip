;(function() {
'use strict';

/* Controllers */

angular.module('app.controllers')
.controller('GuideModalCtrl', [
    '$scope',
    '$modalInstance',
    '$http',
    '$cookies',
    '$cookieStore',
    '$window',
    GuideModalCtrl
]);


function GuideModalCtrl($scope, $modalInstance, $http, $cookies, $cookieStore, $window) {

    console.log($scope.guideShown);

    $scope.closeModal = function(){
      $modalInstance.close();
    }

    $scope.selectGuide = function(guide){
      $scope.$parent.selectedGuide = _.clone(guide);
      $scope.$parent.showOrder = true;
      $scope.$parent.showMap = false;
      $modalInstance.close();
  }

}

}());


