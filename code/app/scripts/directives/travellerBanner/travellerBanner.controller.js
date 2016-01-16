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
    'CitiInfo',
    BannerCtrl
]);


function BannerCtrl($scope, $http, $state, toastr, Controller, TourInfo, CitiInfo) {

	$scope.options = {};

	$http.get(Controller.base() + 'api/city').then(function(res){
		CitiInfo.children = res.data.city;
		TourInfo.city = res.data.city;
		$scope.options.city = _.map(res.data.city, function(city){
			return {
				name: city.cn_name,
				value: city.city_id,
				min: city.min,
				alias: city.alias
			}
		})
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

    	$scope.tourForm.city_plan = _.clone($scope.tourForm.city_plan) || [];
		
		var start_obj = {"city": {"city_id":$scope.tourForm.start_city_id}};
		var end_obj = {"city": {"city_id":$scope.tourForm.end_city_id}};
		var start_index = _($scope.tourForm.city_plan).map(function(city) {
			return city.city.city_id === $scope.tourForm.start_city_id;
		}).compact().value();
		var end_index = _($scope.tourForm.city_plan).map(function(city) {
			return city.city.city_id === $scope.tourForm.end_city_id;
		}).compact().value();

    	if ($scope.tourForm.start_city_id && start_index.length === 0) {   		
			$scope.tourForm.city_plan.unshift(start_obj);    		
    	}
    	if ($scope.tourForm.end_city_id && end_index.length === 0) {
			$scope.tourForm.city_plan.push(end_obj);    		
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
    	$scope.tourForm.one_guide_for_whole_trip = 'BOTH';
    	$scope.tourForm.start_city = start_obj.city;
		$scope.tourForm.end_city = end_obj.city;

;

    	$scope.tourForm = {
			"start_city_id": 1,
			"city_plan": [{
				"city": {
					"city_id": 1
				}
			}, {
				"city": {
					"city_id": 8
				}
			}, {
				"city": {
					"city_id": 2
				}
			}],
			"end_city_id": 2,
			"keep_order_of_via_cities": false,
			"startdate": 20160116,
			"enddate": 20160122,
			"date": {
				"startDate": "2016-01-16T08:00:00.000Z",
				"endDate": "2016-01-23T07:59:59.999Z"
			},
			"num_people": 3,
			"num_room": 3,
			"hotel": 3,
			"one_guide_for_whole_trip": "BOTH",
			"start_city": {
				"city_id": 1
			},
			"end_city": {
				"city_id": 2
			}
		}

    	console.log(JSON.stringify({'itinerary': $scope.tourForm}));

		$http.post(Controller.base() + 'api/plan', {'itinerary': $scope.tourForm}).then(function(res){
			$scope.tourForm.visit_city = [];
			if (res.data && res.data.status === 'SUCCESS') {
				toastr.success('订制成功!');
				TourInfo.itinerary = res.data.itinerary;
				TourInfo.requestData = {'itinerary': $scope.tourForm}
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

