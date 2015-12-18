;(function() {
'use strict';

/* Controllers */

angular.module('app.controllers')
.controller('BannerCtrl', [
    '$scope',
    '$http',
    '$state',
    BannerCtrl
]);


function BannerCtrl($scope, $http, $state) {
    $scope.tourShow = false;
	$scope.tourForm = {};
	$scope.options = {};
	$scope.options.depart = [
		{
			name: '上海'
		},
		{
			name: '北京'
		},
		{
			name: '广州'
		},
		{
			name: '成都'
		},
	];


	$scope.options.des = [
		{
			name: '旧金山'
		},
		{
			name: '纽约'
		},
		{
			name: '洛杉矶'
		},
		{
			name: '迈阿密'
		},
		{
			name: '波士顿'
		},
	];

	$scope.options.middle = [
		{
			name: '旧金山'
		},
		{
			name: '纽约'
		},
		{
			name: '洛杉矶'
		},
		{
			name: '迈阿密'
		},
		{
			name: '波士顿'
		},
	];

	$scope.options.people = [
		{
			name: 1
		},
		{
			name: 2
		},
		{
			name: 3
		},
		{
			name: 4
		},
		{
			name: 5
		},
	];

	$scope.options.topic = [
		{
			name: '旅游'
		},
		{
			name: '蜜月'
		}
	];

	$scope.options.cars = [
		{
			name: '三星'
		},
		{
			name: '两星'
		},
		{
			name: '一星'
		}
	];

	$scope.tourForm.Depart = $scope.options.depart[0].name

    $scope.submitTour = function() {
        $state.go('tour');
    }

    //For Date Picker
	$scope.myDate = new Date();

	$scope.minDate = new Date(
	  $scope.myDate.getFullYear(),
	  $scope.myDate.getMonth() - 2,
	  $scope.myDate.getDate());

	$scope.maxDate = new Date(
	  $scope.myDate.getFullYear(),
	  $scope.myDate.getMonth() + 2,
	  $scope.myDate.getDate());

	$scope.onlyWeekendsPredicate = function(date) {
	var day = date.getDay();
	return day === 0 || day === 6;
	}

}

}());

