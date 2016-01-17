;(function() {
'use strict';

/* Controllers */

angular.module('app.controllers')
.controller('TourCtrl', [
    '$scope',
    '$http',
    '$modal',
    '$state',
    '$rootScope',
    'Controller',
    'TourInfo',
    'toastr',
    'User',
    'AUTH_EVENTS',
    'CitiInfo',
    '$cookieStore',
    TourCtrl
]);


function TourCtrl($scope, $http, $modal, $state, $rootScope, Controller, TourInfo, toastr, User, AUTH_EVENTS, CitiInfo, $cookieStore) {
	
	$scope.TourInfo = TourInfo;
	$scope.$parent.showfooter = false;
	$scope.tours = TourInfo.itinerary.city;
	$scope.requestData = TourInfo.requestData.itinerary;

	$scope.option = {};

	if ($scope.requestData && $scope.requestData.date) {
		$scope.startDate = $scope.requestData.date.startDate.format('YYYY-MM-DD');
		$scope.endDate = $scope.requestData.date.endDate.format('YYYY-MM-DD');
		$scope.diffDate = $scope.requestData.date.endDate.diff($scope.requestData.date.startDate, 'days') + 1;		
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

	$scope.getDays = function(){
		var totalDays = 0;
		_.each($scope.tours, function(tour){
			totalDays += tour.num_days;
		})
		return totalDays;
	}

	$scope.planPlus = function(tour){
		if ($scope.getDays() < $scope.diffDate) {
			tour.num_days++;
			updatePlan(tour, tour.city.city_id, tour.num_days);		
		} else {
			toastr.error('已经达到安排日期上限');
		}
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
			toastr.error(err);
		}) 
	}


	$scope.addPlan = function() {
		TourInfo.requestData.itinerary.city = _.clone($scope.tours);

		var newCity = {
			"city": {
				"city_id": $scope.start_city_id
			}
		}
        var index = _.findIndex(TourInfo.requestData.itinerary.city, function(city) {
            return city.city.city_id === $scope.start_city_id;
        })

		if ($scope.start_city_id && index === -1) {
			TourInfo.requestData.itinerary.city.push(newCity);		
			$http.post(Controller.base() + 'api/plan', TourInfo.requestData).then(function(res){
				console.log(res);
				if (res.data && res.data.status === 'SUCCESS') {
					TourInfo.itinerary = res.data.itinerary;
					$scope.tours = TourInfo.itinerary.city;	
					getTours();
				} else {
					toastr.error('无法添加此城市，请选择其他城市');
					TourInfo.requestData.city = _.clone($scope.tours);
				}
				delete $scope.start_city_id;
			}) 

		} else {
			toastr.error('城市已经存在');
		}
		

	}

	$scope.deletePlan = function(tour) {
		var index = $scope.tours.indexOf(tour);
		$scope.tours.splice(index, 1);
	}

	$scope.chooseGuide = function(){
		
		if ($scope.getDays() !== $scope.diffDate) {
			toastr.error('安排时间和规划时间不符');
			return
		}

		if(!User.persistentData.loggedIn) {
			toastr.error('未登录，请先登录');
			$rootScope.$broadcast(AUTH_EVENTS.notAuthenticated);
			return
		}

		chooseGuideFunc();
	}


	$rootScope.$on(AUTH_EVENTS.loginSuccess, function(){
		chooseGuideFunc();
	});

	function chooseGuideFunc() {
		$scope.showGuide = true;
		$scope.showMap = false;

		var obj = {};
		obj.itinerary = {
			"start_date": $scope.requestData.startdate,
			"end_date": $scope.requestData.enddate,
			"startdate": $scope.requestData.startdate,
			"enddate": $scope.requestData.enddate,
	        "one_guide_for_whole_trip": "BOTH",
	        "hotel": $scope.requestData.hotel,
	        "num_people": $scope.requestData.num_people,
	        "num_room": $scope.requestData.num_room,		
		}

		obj.itinerary.city = _.map($scope.tours, function(tour){
			return {
				"city": 
					{
					"city_id": tour.city.city_id
					},
				"num_days": tour.num_days
			}
		});

		obj.itinerary.start_city= obj.itinerary.city[0].city;
		obj.itinerary.end_city = obj.itinerary.city[obj.itinerary.city.length - 1].city;
		// obj.visit_city = _.clone(obj.city_plan);
		
		$http.post(Controller.base() + 'api/plan', obj).then(function(res){
			if(res.data.status === 'SUCCESS') {
				TourInfo.itinerary = res.data.itinerary;
				obj.itinerary.edge = TourInfo.itinerary.edge;
				// console.log(JSON.stringify(obj));
				//Now the shit
				var objcopy = _.clone(obj.itinerary);
				objcopy.city_plan = _.clone(objcopy.city);
				$http.post(Controller.base() + 'api/guide', objcopy).then(function(res){
					parseGuideInfo(res.data.guide_plan);
				}) 					
			} else {
				console.log(res.data.status);
			}		
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
		var obj = {
			"end_date": $scope.requestData.startdate,
			"start_date": $scope.requestData.startdate,
	        "hotel": $scope.requestData.hotel,
	        "num_people": $scope.requestData.num_people,
	        "num_room": $scope.requestData.num_room,		
		}


		obj.itinerary = {};
		obj.itinerary.edge = TourInfo.itinerary.edge;

		if ($scope.chooseGuideTypeStatus === "one") {
			// obj.selectedGuideId = $scope.selectedGuide.guide_id;
			obj.itinerary.city = _.map($scope.tours, function(tour){
				return {
					"city": 
						{
						"city_id": tour.city.city_id
						},
					"num_days": tour.num_days,
					"guide": 
						{
						"host_city": {"city_id" : $scope.selectedGuide.host_city.city_id}
						}
				}
			});
			// obj.guide_plan_type = "ONE_GUIDE_FOR_THE_WHOLE_TRIP";
			obj.itinerary["guide_for_whole_trip"] = $scope.selectedGuide;
			obj.itinerary['choose_one_guide_solution'] = true;
		} else if ($scope.chooseGuideTypeStatus === "multi") {
			obj.itinerary.city = _.values($scope.multi_city_plan);
			obj.itinerary['choose_one_guide_solution'] = false;
		}

		$scope.quotes = [];
		// console.log(JSON.stringify(obj));
		$http.post(Controller.base() + 'api/quote', obj).then(function(res){
			TourInfo.itinerary = res.data.itinerary;
			if(obj.guide_plan_type = "ONE_GUIDE_FOR_EACH_CITY") {
				$scope.quotes = res.data.itinerary.quote_for_multiple_guides.cost_usd;
			} else {
				$scope.quotes = res.data.itinerary.guide_for_whole_trip.cost_usd;
			}
			$scope.showQuoteView = true;
			$scope.quoteToPay = "预览最终行程并支付";
		}).catch(function(e){
			console.log(e);
		}) 		
	}

	$scope.gotoReview = function(quote){
		TourInfo.itinerary.user_token = $cookieStore.get('token');
		// console.log(JSON.stringify(TourInfo.itinerary));
		$http.post(Controller.base() + 'api/booking', {"itinerary": TourInfo.itinerary}).then(function(res){
			if(res.data.status === 'SUCCESS') {
				TourInfo.itinerary = res.data.itinerary;
				$state.go('review');
			}
			
		}).catch(function(err){
			console.log(err);
		});
		
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
		delete $scope.multi_city_plan[plan.plan.city.city_id];
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