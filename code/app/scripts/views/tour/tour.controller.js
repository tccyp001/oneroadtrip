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

	_.each($scope.tours, function(tour){
		var id = tour.city_id;
		var num_days = tour.num_days;
		$http.post(Controller.base() + 'api/spot', {
			'city_id': 8,
			'num_days': 3
		}).then(function(res){
			tour.plans = res.data.day_plan;
		}) 
	})



	$scope.planPlus = function(plan){
		plan.num_days++;
		updatePlan(plan.city_id, plan.num_days);
	}


	$scope.planMinus = function(plan){
		if(plan.num_days > 0) {
			plan.num_days--;
		}
		updatePlan(plan.city_id, plan.num_days);
	}

	function updatePlan(id, days) {
		console.log(id, days);
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






	$scope.tour = {
		data: [
			{
				'city': '洛杉矶',
				'date': '3',
				'pos': [40.74, -74.18],
				'content': [
					'洛杉矶是加州的第一大城（拥有超过400万人口），也是美国的第二大城[8]，仅次于纽约市。它的面积为469.1平方英里（1214.9平方公里）。洛杉矶-长滩-圣安娜都会区拥有1300万人口。',
					'洛杉矶是加州的第一大城（拥有超过400万人口），是美国的第二大城[8]，仅次于纽约市。它的面积为469.1平方英里（1214.9平方公里）。洛杉矶-长滩-圣安娜都会区拥有1300万人口。',
					'洛杉矶是加州的第一大城（拥有超过400万人口），是美国第二大城[8]，仅次于纽约市。它的面积为469.1平方英里（1214.9平方公里）。洛杉矶-长滩-圣安娜都会区拥有1300万人口。'
				]
			},
			{
				'city': '旧金山',
				'date': '3',
				'pos': [45.74, -76.18],
				'content': [
					'洛杉矶是加州的第一大城（拥有超过400万人口），也是美国的第二大城[8]，仅次于纽约市。它的面积为469.1平方英里（1214.9平方公里）。洛杉矶-长滩-圣安娜都会区拥有1300万人口。',
					'洛杉矶是加州的第一大城（拥有超过400万人口），是美国的第二大城[8]，仅次于纽约市。它的面积为469.1平方英里（1214.9平方公里）。洛杉矶-长滩-圣安娜都会区拥有1300万人口。',
					'洛杉矶是加州的第一大城（拥有超过400万人口），是美国第二大城[8]，仅次于纽约市。它的面积为469.1平方英里（1214.9平方公里）。洛杉矶-长滩-圣安娜都会区拥有1300万人口。'
				]
			}
		],
		guideList: [
			{
				'name':'Peter',
				"photo_url": '/images/guide/person.png',
				'score': 4,
				'description': '西雅图金牌导游，熟悉西雅图所有景点，带你品尝西雅图美食。全美国带团，精通黄石公园、大峡谷、拉斯、旧金山、落山矶、1号公路等经典线路。',
				'experience': 4,
				'drive_year': 6,
				'has_car': true,
				'max_people': 6,
				'language': '中文 英语',
				'citizenship': '美国',
				'price_usd': 12000,
				'price_cny': 80000,
				'car': '豪华七座商务车',
				'tag': ['幽默', '帅气'],
				'city': '旧金山，洛杉矶，拉斯维加斯',
				'review': '玩的很开心，谢谢导游'
			},
			{
				'name':'Wang',
				"photo_url": '/images/guide/person.png',
				'score': 4,
				'description': '西雅图金牌导游，熟悉西雅图所有景点，带你品尝西雅图美食。全美国带团，精通黄石公园、大峡谷、拉斯、旧金山、落山矶、1号公路等经典线路。',
				'experience': 4,
				'drive_year': 6,
				'has_car': true,
				'max_people': 6,
				'language': '中文 英语',
				'citizenship': '美国',
				'price_usd': 12000,
				'price_cny': 80000,
				'car': '豪华七座商务车',
				'tag': ['幽默', '帅气'],
				'city': '旧金山，洛杉矶，拉斯维加斯',
				'review': '玩的很开心，谢谢导游'
			},
			{
				'name':'Victor',
				"photo_url": '/images/guide/person.png',
				'score': 4,
				'description': '西雅图金牌导游，熟悉西雅图所有景点，带你品尝西雅图美食。全美国带团，精通黄石公园、大峡谷、拉斯、旧金山、落山矶、1号公路等经典线路。',
				'experience': 4,
				'drive_year': 6,
				'has_car': true,
				'max_people': 6,
				'language': '中文 英语',
				'citizenship': '美国',
				'price_usd': 12000,
				'price_cny': 80000,
				'car': '豪华七座商务车',
				'tag': ['幽默', '帅气'],
				'city': '旧金山，洛杉矶，拉斯维加斯',
				'review': '玩的很开心，谢谢导游'
			},
			{
				'name':'Jason',
				"photo_url": '/images/guide/person.png',
				'score': 4,
				'description': '西雅图金牌导游，熟悉西雅图所有景点，带你品尝西雅图美食。全美国带团，精通黄石公园、大峡谷、拉斯、旧金山、落山矶、1号公路等经典线路。',
				'experience': 4,
				'drive_year': 6,
				'has_car': true,
				'max_people': 6,
				'language': '中文 英语',
				'citizenship': '美国',
				'price_usd': 12000,
				'price_cny': 80000,
				'car': '豪华七座商务车',
				'tag': ['幽默', '帅气'],
				'city': '旧金山，洛杉矶，拉斯维加斯',
				'review': '玩的很开心，谢谢导游'
			}


		]
	}


	$scope.toggleContent = function(plan){
		plan.contentStatus = !plan.contentStatus;
	}

	$scope.showMap = true; 

	$scope.chooseGuide = function(){
		$scope.showGuide = true;
		$scope.showMap = false;
	}

	$scope.selectGuide = function(guide){
		$scope.selectedGuide = _.clone(guide);
		$scope.showOrder = true;
		$scope.showMap = false;
	}

	$scope.cancelSelectedGuide = function(){
		$scope.selectedGuide = {};
		$scope.showOrder = false;
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