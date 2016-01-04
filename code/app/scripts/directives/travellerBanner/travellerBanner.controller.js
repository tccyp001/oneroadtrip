;(function() {
'use strict';

/* Controllers */

angular.module('app.controllers')
.controller('BannerCtrl', [
    '$scope',
    '$http',
    '$state',
    'toastr',
    'Controller',
    'TourInfo',
    BannerCtrl
]);


function BannerCtrl($scope, $http, $state, toastr, Controller, TourInfo) {

	$scope.options = {};

	$http.get(Controller.base() + 'api/city').then(function(res){
		$scope.options.city = _.map(res.data.city, function(city){
			return {
				name: city.cn_name,
				value: city.city_id,
				min: city.min,
				alias: city.alias
			}
		})
		TourInfo.city = $scope.options.city;
	});

	$scope.datePicker = {
		date: {startDate: null, endDate: null}
	};


    $scope.tourShow = false;
	$scope.tourForm = {};

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

	$scope.options.hotel = [
		{
			name: '五星级',
			value: '5'
		},
		{
			name: '四星级',
			value: '4'
		},
		{
			name: '三星级',
			value: '3'
		},
		{
			name: '两星级',
			value: '2'
		},
		{
			name: '一星级',
			value: '1'
		}
	];


    $scope.submitTour = function() {

    	$scope.tourForm.visit_city = $scope.tourForm.visit_city || [];
    	if ($scope.tourForm.start_city_id) {
			$scope.tourForm.visit_city.unshift({"city": {"city_id":$scope.tourForm.start_city_id}});    		
    	}
    	if ($scope.tourForm.end_city_id) {
			$scope.tourForm.visit_city.push({"city": {'city_id': $scope.tourForm.end_city_id}});    		
    	}

    	$scope.tourForm.keep_order_of_via_cities = false;

    	if ($scope.datePicker.date.startDate && $scope.datePicker.date.endDate) {
	    	$scope.tourForm.startdate = parseInt($scope.datePicker.date.startDate.format('YYYYMMDD'));
	    	$scope.tourForm.enddate = parseInt($scope.datePicker.date.endDate.format('YYYYMMDD'));
	    	$scope.tourForm.date = $scope.datePicker.date;
    	}


    	$scope.tourForm.num_people = parseInt($scope.tourForm.num_people);
    	$scope.tourForm.num_room = parseInt($scope.tourForm.num_room);
    	$scope.tourForm.hotel = parseInt($scope.tourForm.hotel);


    	$scope.tourForm = {
    		"end_city_id": 8,
			"enddate": 20160113,
			"hotel": 5,
			"keep_order_of_via_cities": false,
			"num_people": 3,
			"num_room": 2,
			"start_city_id": 1,
			"startdate": 20160107,
			"visit_city": [
				{ "city": {"city_id": 1} },
		      	{ "city": {"city_id": 2} },
		      	{ "city": {"city_id": 8} }
		      	],
			"date": $scope.datePicker.date
    	}

		$http.post(Controller.base() + 'api/plan', $scope.tourForm).then(function(res){
			if (res.data && res.data.status === 'SUCCESS') {
				// $scope.tourForm.visit_city = [];
				// $scope.tourForm.start_city_id = $scope.tourForm.end_city_id = undefined;
				toastr.success('订制成功!');
				TourInfo.data = res.data;
				TourInfo.requestData = $scope.tourForm;
				$state.go('tour');				
			} else {
				toastr.error('订制失败，请重新尝试');
			}
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

