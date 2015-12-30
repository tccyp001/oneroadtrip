;(function() {
'use strict';

/* Controllers */

angular.module('app.controllers')
.controller('BannerCtrl', [
    '$scope',
    '$http',
    '$state',
    'Controller',
    'TourInfo',
    BannerCtrl
]);


function BannerCtrl($scope, $http, $state, Controller, TourInfo) {

	$scope.options = {};

	$http.post(Controller.base() + 'api/city', {}).then(function(res){
		$scope.options.city = res.data.city;
	}) 

	$scope.datePicker = {
		date: {startDate: null, endDate: null}
	};


    $scope.tourShow = false;
	$scope.tourForm = {};



	$scope.options.depart = [
		{
			name: '西雅图'
		},
		{
			name: '旧金山'
		},
		{
			name: '洛杉矶'
		},
		{
			name: '拉斯维加斯'
		},
		{
			name: '盐湖城'
		},
		{
			name: '黄石'
		},
		{
			name: '丹佛'
		},
		{
			name: '休斯顿'
		},
		{
			name: '纽约'
		},
		{
			name: '华盛顿'
		},
		{
			name: '波士顿'
		},
		{
			name: '迈阿密'
		},
		{
			name: '夏威夷'
		},
		{
			name: '芝加哥'
		},
		{
			name: '亚特兰大'
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
			name: '四星级'
		},
		{
			name: '三星级'
		},
		{
			name: '两星级'
		},
		{
			name: '一星级'
		}
	];



    $scope.submitTour = function() {

    	$scope.tourForm.visit_city = $scope.tourForm.visit_city || [];
		$scope.tourForm.visit_city.unshift({'city_id':$scope.tourForm.start_city_id});
		$scope.tourForm.visit_city.push({'city_id': $scope.tourForm.end_city_id});

    	$scope.tourForm.keep_order_of_via_cities = false;

    	$scope.tourForm.startDate = $scope.datePicker.date.startDate.format('YYYYMMDD');
    	$scope.tourForm.endDate = $scope.datePicker.date.endDate.format('YYYYMMDD');

    	console.log($scope.tourForm);

		$http.post(Controller.base() + 'api/plan', $scope.tourForm).then(function(res){
			TourInfo.data = res.data;
			$state.go('tour');
		}) 
       
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

