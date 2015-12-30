;(function() {
'use strict';

/* Controllers */

angular.module('app.controllers')
.controller('GuideCtrl', [
    '$scope',
    '$http',
    '$modal',
    'Controller',
    'TourInfo',
    GuideCtrl
]);


function GuideCtrl($scope, $http, $modal, Controller, TourInfo) {
	$scope.$parent.showfooter = true;
	$scope.guideShown = {
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
	}
}

}());