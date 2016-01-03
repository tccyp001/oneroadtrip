;(function() {
'use strict';

/* Controllers */

angular.module('app.controllers')
.controller('TourCtrl', [
    '$scope',
    '$http',
    '$modal',
    'Controller',
    'TourInfo',
    'toastr',
    TourCtrl
]);


function TourCtrl($scope, $http, $modal, Controller, TourInfo, toastr) {
	
	$scope.TourInfo = TourInfo;
	$scope.$parent.showfooter = false;
	$scope.tours = TourInfo.data.visit;
	$scope.requestData = TourInfo.requestData;
	$scope.option = {};

	if (TourInfo.requestData && TourInfo.requestData.date) {
		$scope.startDate = TourInfo.requestData.date.startDate.format('YYYY-MM-DD');
		$scope.endDate = TourInfo.requestData.date.endDate.format('YYYY-MM-DD');
		$scope.diffDate = TourInfo.requestData.date.endDate.diff(TourInfo.requestData.date.startDate, 'days');		
	}

	$scope.dragmoved = function(index) {
		$scope.tours.splice(index, 1);
	}


	getTours();
	function getTours() {
		_.each($scope.tours, function(tour){
			var id = tour.city.city_id;
			var num_days = tour.num_days;
			updatePlan(tour, id, num_days);
		})
	}


	$scope.planPlus = function(tour){
		tour.num_days++;
		updatePlan(tour, tour.city.city_id, tour.num_days);
	}


	$scope.planMinus = function(tour){
		if(tour.num_days > 0) {
			tour.num_days--;
		}
		updatePlan(tour, tour.city.city_id, tour.num_days);
	}

	function updatePlan(tour, id, days) {
		$http.post(Controller.base() + 'api/spot', {
			'city_id': id,
			'num_days': days
		}).then(function(res){
			tour.plans = res.data.day_plan;
		}, function(err){
			console.log(err);
		}) 
	}

	$scope.addPlan = function() {
		TourInfo.requestData.visit_city = _.clone($scope.tours);
		if ($scope.start_city_id) {
			var newCity = {
				"city": {
					"city_id": $scope.start_city_id
				}
			}
			TourInfo.requestData.visit_city.push(newCity);		
			$http.post(Controller.base() + 'api/plan', TourInfo.requestData).then(function(res){
				if (res.data && res.data.status === 'SUCCESS') {
					// $scope.tourForm.visit_city = [];
					// $scope.tourForm.start_city_id = $scope.tourForm.end_city_id = undefined;
					
					TourInfo.data = res.data;
					$scope.tours = TourInfo.data.visit;	
					getTours();
				} else {
					toastr.error('无法添加此城市，请选择其他城市');
					TourInfo.requestData.visit_city = _.clone($scope.tours);
				}
				delete $scope.start_city_id;
			}) 

		}
		

	}

	$scope.deletePlan = function(tour) {
		var index = $scope.tours.indexOf(tour);
		$scope.tours.splice(index, 1);
	}

	$scope.chooseGuide = function(){
			
		$scope.showGuide = true;
		$scope.showMap = false;

		var obj = {
			"start_date": $scope.requestData.startdate,
	        "one_guide_for_whole_trip": "BOTH",
	        "hotel": $scope.requestData.hotel,
	        "num_people": $scope.requestData.num_people,
	        "num_room": $scope.requestData.num_room,		
		}

		obj.city_plan = _.map($scope.tours, function(tour){
			return {
				"city": 
					{
					"city_id": tour.city.city_id
					},
				"num_days": tour.num_days
			}
		});

		$http.post(Controller.base() + 'api/guide', obj).then(function(res){
			parseGuideInfo(res.data.guide_plan);
		}) 
	}

	// Default view to show one
	$scope.chooseGuideTypeStatus = 'one';
	$scope.chooseGuideType = function(type){
		$scope.chooseGuideTypeStatus = type;
	}

	$scope.showGuideContentStatus = false;
	$scope.toggleGuideContent = function(plan){
		plan.showGuideContentStatus = !plan.showGuideContentStatus;
	}


	function parseGuideInfo(data){
		_.each(data, function(item) {
			if (item.guide_plan_type === "ONE_GUIDE_FOR_EACH_CITY") {
				$scope.guideInfo_Multi = item.city_plan;
			} else if(item.guide_plan_type === "ONE_GUIDE_FOR_THE_WHOLE_TRIP") {
				$scope.guideInfo = item.guide_for_whole_trip;
			}
		})
	}


	$scope.quotes = [
		{
			"value": "10000/1"
		},
		{
			"value": "20000/2"
		},
		{
			"value": "24000/3"
		},
		{
			"value": "28000/4"
		}
	]

	$scope.multi_city_plan = {};
	$scope.selectGuide = function(guide, plan){
		if ($scope.chooseGuideTypeStatus === 'one') {
			$scope.selectedGuide = _.clone(guide);
		} else if ($scope.chooseGuideTypeStatus === 'multi'){
			var plan_copy = _.clone(guide);
			$scope.multi_city_plan[plan.city.city_id] = {
				"plan": plan,
				'guide': guide
			};
		}
		$scope.showOrder = true;
		$scope.showMap = false;
		resetQuote();

	}


	$scope.getQuote = function(){
		$scope.showQuoteView = true;
		$scope.quoteToPay = "预览最终行程并支付";

		var obj = {
			"start_date": $scope.requestData.startdate,
	        "hotel": $scope.requestData.hotel,
	        "num_people": $scope.requestData.num_people,
	        "num_room": $scope.requestData.num_room,		
		}

		if ($scope.chooseGuideTypeStatus === "one") {
			obj.selectedGuideId = $scope.selectedGuide.id; 
			obj.guide_plan_type = "ONE_GUIDE_FOR_THE_WHOLE_TRIP";
		} else if ($scope.chooseGuideTypeStatus === "multi") {
			obj.guide_plan_type = "ONE_GUIDE_FOR_EACH_CITY";
			obj.visit_plan = _.values($scope.multi_city_plan);
		}

		// $http.post(Controller.base() + 'api/quote', obj).then(function(res){
		// 	console.log(res);
		// }) 		
	}

	$scope.gotoReview = function(){
		console.log(quote);
	}


	$scope.openGuideModal = function(guide){
		$scope.guideShown = guide;
	    var guideModalInstance = $modal.open({
	      animation: true,
	      scope: $scope,
	      templateUrl: 'scripts/directives/modal/guideModal.tpl.html',
	      controller: 'GuideModalCtrl',
	    });
	}

	$scope.toggleContent = function(plan){
		plan.contentStatus = !plan.contentStatus;
	}

	$scope.showMap = true; 


	resetQuote();
	function resetQuote(){
		$scope.quoteToPay = "获取价格";
		$scope.showQuoteView = false;			
	}

	$scope.cancelSelectedGuide = function(){
		$scope.selectedGuide = {};
		$scope.showOrder = false;
		resetQuote();
	}

	$scope.cancelGuideFromList = function(plan) {
		delete $scope.multi_city_plan[plan.city_id];
		resetQuote();
	}

	$scope.gotoStep = function(step) {
		switch(step){
			case 1:
				$scope.showMap = true; 
				$scope.showOrder = false;
				$scope.showGuide = false;
				break;
			case 2:
				$scope.showMap = false; 
				$scope.showOrder = false;
				$scope.showGuide = true;
		}
	}

}

}());