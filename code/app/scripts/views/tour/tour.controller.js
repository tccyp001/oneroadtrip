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
    TourCtrl
]);


function TourCtrl($scope, $http, $modal, Controller, TourInfo) {
	
	$scope.$parent.showfooter = false;
	$scope.tours = TourInfo.data.visit;
	$scope.requestData = TourInfo.requestData;

	if (TourInfo.requestData && TourInfo.requestData.date) {
		$scope.startDate = TourInfo.requestData.date.startDate.format('YYYY-MM-DD');
		$scope.endDate = TourInfo.requestData.date.endDate.format('YYYY-MM-DD');
		$scope.diffDate = TourInfo.requestData.date.endDate.diff(TourInfo.requestData.date.startDate, 'days');		
	}

	$scope.dragmoved = function(index) {
		$scope.tours.splice(index, 1);
	}

	_.each($scope.tours, function(tour){
		var id = tour.city.city_id;
		var num_days = tour.num_days;
		updatePlan(tour, id, num_days);
	})

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
			console.log(res);
			tour.plans = res.data.day_plan;
		}, function(err){
			console.log(err);
		}) 
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
				// _.each(item.city_plan, function(plan){
				// 	plan.guide_info = {};
				// 	_.each(plan.guide_id, function(id) {
				// 		$http.get(Controller.base() + 'api/guideinfo/' + id).then(function(res){
				// 			plan.guide_info[id] = res.data.info;
				// 		}) 						
				// 	})
				// });
				console.log($scope.guideInfo_Multi);
			} else if(item.guide_plan_type === "ONE_GUIDE_FOR_THE_WHOLE_TRIP") {
				$scope.guideInfo = item.guide_for_whole_trip;
				// item.guide_info = {};
				// _.each(item.guide_id_for_whole_trip, function(id){
				// 		$http.get(Controller.base() + 'api/guideinfo/' + id).then(function(res){
				// 			item.guide_info[id] = res.data.info;
				// 		}) 						
				// })
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
		console.log(guide, plan);
		if ($scope.chooseGuideTypeStatus === 'one') {
			$scope.selectedGuide = _.clone(guide);
		} else if ($scope.chooseGuideTypeStatus === 'multi'){
			var plan_copy = _.clone(guide);
			$scope.multi_city_plan[plan.city.city_id] = {
				"plan": plan,
				'guide': guide
			};
		}
		console.log($scope.multi_city_plan);
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