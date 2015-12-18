;(function() {
'use strict';

/* Controllers */

angular.module('app.controllers')
.controller('TourCtrl', [
    '$scope',
    '$http',
    TourCtrl
]);


function TourCtrl($scope, $http) {
	$scope.tour = {
		data: [
			{
				'city': '洛杉矶',
				'date': '3',
				'content': [
					'洛杉矶是加州的第一大城（拥有超过400万人口），也是美国的第二大城[8]，仅次于纽约市。它的面积为469.1平方英里（1214.9平方公里）。洛杉矶-长滩-圣安娜都会区拥有1300万人口。',
					'洛杉矶是加州的第一大城（拥有超过400万人口），是美国的第二大城[8]，仅次于纽约市。它的面积为469.1平方英里（1214.9平方公里）。洛杉矶-长滩-圣安娜都会区拥有1300万人口。',
					'洛杉矶是加州的第一大城（拥有超过400万人口），是美国第二大城[8]，仅次于纽约市。它的面积为469.1平方英里（1214.9平方公里）。洛杉矶-长滩-圣安娜都会区拥有1300万人口。'
				]
			},
			{
				'city': '旧金山',
				'date': '3',
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
				'description': '这是个好导游',
				'experience': 4,
				'has_car': true,
				'max_people': 6,
				'language': '中文',
				'citizenship': '美国',
				'price_usd': 12000,
				'price_cny': 80000
			},
			{
				'name':'Wang',
				"photo_url": '/images/guide/person.png',
				'score': 4,
				'description': '这是个好导游',
				'experience': 4,
				'has_car': true,
				'max_people': 6,
				'language': '中文',
				'citizenship': '美国',
				'price_usd': 12000,
				'price_cny': 80000
			},
			{
				'name':'Victor',
				"photo_url": '/images/guide/person.png',
				'score': 4,
				'description': '这是个好导游',
				'experience': 4,
				'has_car': true,
				'max_people': 6,
				'language': '中文',
				'citizenship': '美国',
				'price_usd': 12000,
				'price_cny': 80000
			},
			{
				'name':'Jason',
				"photo_url": '/images/guide/person.png',
				'score': 4,
				'description': '这是个好导游',
				'experience': 4,
				'has_car': true,
				'max_people': 6,
				'language': '中文',
				'citizenship': '美国',
				'price_usd': 12000,
				'price_cny': 80000
			}


		]
	}


	$scope.toggleContent = function(tour, key){
		tour[key] = (tour[key])? tour[key] : {};
		tour[key].contentStatus = !tour[key].contentStatus;
	}

	$scope.showMap = false; 

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