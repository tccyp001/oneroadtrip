;(function() {
'use strict';

/* Controllers */

angular.module('app.controllers')
.controller('ReviewCtrl', [
    '$scope',
    '$http',
    '$state',
    '$location',
    '$timeout',
    'TourInfo',
    'Controller',
    ReviewCtrl
]);


function ReviewCtrl($scope, $http, $state, $location, $timeout, TourInfo, Controller) {

	$scope.$parent.showfooter = true;

	$scope.TourInfo = TourInfo;

// 	var FakeTourInfo = {
// 	"data": {
// 		"status": "SUCCESS",
// 		"itinerary": {
// 			"city": [{
// 				"city": {
// 					"city_id": 1
// 				},
// 				"num_days": 2,
// 				"guide": [{
// 					"host_city": {
// 						"city_id": 8
// 					}
// 				}]
// 			}, {
// 				"city": {
// 					"city_id": 2
// 				},
// 				"num_days": 3,
// 				"guide": [{
// 					"host_city": {
// 						"city_id": 8
// 					}
// 				}]
// 			}, {
// 				"city": {
// 					"city_id": 8
// 				},
// 				"num_days": 3,
// 				"guide": [{
// 					"host_city": {
// 						"city_id": 8
// 					}
// 				}]
// 			}],
// 			"edge": [{
// 				"from_city": {
// 					"city_id": 1,
// 					"name": "San Diego",
// 					"cn_name": "圣地亚哥"
// 				},
// 				"to_city": {
// 					"city_id": 1,
// 					"name": "San Diego",
// 					"cn_name": "圣地亚哥"
// 				},
// 				"distance": 0,
// 				"hours": 0
// 			}, {
// 				"from_city": {
// 					"city_id": 1,
// 					"name": "San Diego",
// 					"cn_name": "圣地亚哥"
// 				},
// 				"to_city": {
// 					"city_id": 2,
// 					"name": "Los Angels",
// 					"cn_name": "洛杉矶"
// 				},
// 				"distance": 120,
// 				"hours": 2
// 			}, {
// 				"from_city": {
// 					"city_id": 2,
// 					"name": "Los Angels",
// 					"cn_name": "洛杉矶"
// 				},
// 				"to_city": {
// 					"city_id": 8,
// 					"name": "San Francisco",
// 					"cn_name": "旧金山"
// 				},
// 				"distance": 383,
// 				"hours": 7
// 			}, {
// 				"from_city": {
// 					"city_id": 8,
// 					"name": "San Francisco",
// 					"cn_name": "旧金山"
// 				},
// 				"to_city": {
// 					"city_id": 8,
// 					"name": "San Francisco",
// 					"cn_name": "旧金山"
// 				},
// 				"distance": 0,
// 				"hours": 0
// 			}],
// 			"quote_for_multiple_guides": {
// 				"cost_usd": 4209,
// 				"route_cost": 1509,
// 				"hotel_cost": 2200,
// 				"hotel_cost_for_guide": 500
// 			}
// 		}
// 	},
// 	"requestData": {
// 		"end_city_id": 8,
// 		"enddate": 20160113,
// 		"hotel": 5,
// 		"keep_order_of_via_cities": false,
// 		"num_people": 3,
// 		"num_room": 2,
// 		"start_city_id": 1,
// 		"startdate": 20160107,
// 		"visit_city": [],
// 		"date": {
// 			"startDate": "2016-01-14T08:00:00.000Z",
// 			"endDate": "2016-01-22T07:59:59.999Z"
// 		}
// 	},
// 	"city": [{
// 		"name": "圣地亚哥",
// 		"value": 1,
// 		"min": 1,
// 		"alias": ["San Diego", "圣地亚哥", "SD", "SDYG", "SHIDIYAGE", "SAN"]
// 	}, {
// 		"name": "洛杉矶",
// 		"value": 2,
// 		"min": 2,
// 		"alias": ["Los Angels", "洛杉矶", "LA", "LUOSANJI", "LDJ", "LAX"]
// 	}, {
// 		"name": "拉斯维加斯",
// 		"value": 3,
// 		"min": 2,
// 		"alias": ["Las Vegas", "拉斯维加斯", "LV", "LSWJS", "LaSiWeiJiaSi"]
// 	}, {
// 		"name": "菲尼克斯",
// 		"value": 4,
// 		"min": 1,
// 		"alias": ["Phoenix", "菲尼克斯", "FNKS", "FeiNiKeSi"]
// 	}, {
// 		"name": "盐湖城",
// 		"value": 5,
// 		"min": 1,
// 		"alias": ["Salt Lake City", "盐湖城", "SLC", "YHC", "YanHuCheng"]
// 	}, {
// 		"name": "雷诺",
// 		"value": 6,
// 		"min": 1,
// 		"alias": ["Reno", "雷诺", "LN", "LeiNuo"]
// 	}, {
// 		"name": "萨克拉门托",
// 		"value": 7,
// 		"min": 1,
// 		"alias": ["Sacramento", "萨克拉门托", "SKLMT", "SaKeLaMenTuo"]
// 	}, {
// 		"name": "旧金山",
// 		"value": 8,
// 		"min": 1,
// 		"alias": ["San Francisco", "旧金山", "SF", "JJS", "JIUJINGSHAN", "SFO"]
// 	}, {
// 		"name": "波特兰",
// 		"value": 9,
// 		"min": 1,
// 		"alias": ["Portland", "波特兰", "BTL", "BoTeLan"]
// 	}, {
// 		"name": "西雅图",
// 		"value": 10,
// 		"min": 1,
// 		"alias": ["Seattle", "西雅图", "XYT", "XIYATU", "SEA"]
// 	}, {
// 		"name": "温哥华",
// 		"value": 11,
// 		"min": 1,
// 		"alias": ["Vancouvor", "温哥华", "WGH", "WenGeHua"]
// 	}, {
// 		"name": "芝加哥",
// 		"value": 12,
// 		"min": 2,
// 		"alias": ["Chicago", "芝加哥", "ZHIJIAGE", "ZHJG", "ORD"]
// 	}, {
// 		"name": "纽约",
// 		"value": 13,
// 		"min": 2,
// 		"alias": ["New York", "纽约", "NY", "NiuYue"]
// 	}, {
// 		"name": "华盛顿",
// 		"value": 14,
// 		"min": 1,
// 		"alias": ["Washington", "华盛顿", "HSD", "HuaShengDun", "DC"]
// 	}, {
// 		"name": "费城",
// 		"value": 15,
// 		"min": 1,
// 		"alias": ["Philadelphia", "费城", "FC", "FeiCheng"]
// 	}, {
// 		"name": "波士顿",
// 		"value": 16,
// 		"min": 1,
// 		"alias": ["Boston", "波士顿", "BSD", "BoShiDun"]
// 	}, {
// 		"name": "迈阿密",
// 		"value": 17,
// 		"min": 2,
// 		"alias": ["Miami", "迈阿密", "MAM", "MaiAMi"]
// 	}, {
// 		"name": "亚特兰大",
// 		"value": 18,
// 		"min": 1,
// 		"alias": ["Atlanta", "亚特兰大", "YTLD", "YaTeLanDa"]
// 	}, {
// 		"name": "奥兰多",
// 		"value": 19,
// 		"min": 2,
// 		"alias": ["Orlando", "奥兰多", "ALD", "AoLanDuo"]
// 	}, {
// 		"name": "休斯顿",
// 		"value": 20,
// 		"min": 2,
// 		"alias": ["Houston", "休斯顿", "XSD", "XiuSiDun"]
// 	}, {
// 		"name": "丹佛",
// 		"value": 21,
// 		"min": 1,
// 		"alias": ["Denver", "丹佛", "DF", "DanFo"]
// 	}, {
// 		"name": "新奥尔良",
// 		"value": 22,
// 		"min": 1,
// 		"alias": ["New Orleans", "新奥尔良", "NO", "XAEL", "XinAoErLiang"]
// 	}, {
// 		"name": "夏威夷",
// 		"value": 23,
// 		"min": 2,
// 		"alias": ["Hawaii", "夏威夷", "HI", "XWY", "XIAWEIYI"]
// 	}, {
// 		"name": "哥伦布",
// 		"value": 24,
// 		"min": 1,
// 		"alias": ["Columbus", "哥伦布", "GLB", "GeLunBu", "COLO"]
// 	}, {
// 		"name": "大瀑布",
// 		"value": 25,
// 		"min": 1,
// 		"alias": ["Niagara Fall", "大瀑布", "NF", "NiYaJiaLa", "NYJL", "DPB", "DaPuBu"]
// 	}, {
// 		"name": "达拉斯",
// 		"value": 26,
// 		"min": 1,
// 		"alias": ["Dallas", "达拉斯", "DLS", "DaLaSi"]
// 	}]
// }

	$scope.itinerary = TourInfo.itinerary;


	_.each($scope.itinerary.city, function(city){
		var id = city.city.city_id;
		var num_days = city.num_days;
		updatePlan(city, id, num_days);
	})


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
	
	// var handler = StripeCheckout.configure({
	//   name: "Custom Example",
	//   token: function(token, args) {
	//     $log.debug("Got stripe token: " + token.id);
	//   }
	// });


	// $scope.doCheckout = function(token, args) {
	// 	var options = {
	// 	  description: "Ten dollahs!",
	// 	  amount: 1000
	// };

	$scope.doCheckout = function(token) {
		console.log(token);
		console.log(TourInfo);

		TourInfo.itinerary.order.token = token.id;
		console.log(JSON.stringify({"itinerary": TourInfo.itinerary}));
		$http.post(Controller.base() + 'api/order', {"itinerary": TourInfo.itinerary}).then(function(res){
			console.log(res);
			if(res.data.status === 'SUCCESS') {
				$state.go('comfirm');
			}
			
		}).catch(function(err){
			console.log(err);
		});
    };

	// $scope.parameters = $location.absUrl();

	// $scope.$watch('parameters', function(val) {
	// 	if(val.indexOf('stripeToken') > -1) {
	// 		var url = decodeURIComponent(val);
	// 		var urlParams = url.split('?')[1].split('#')[0].split('&');
	// 		var path = url.split('?')[1].split('#')[1];
	// 		makePaymentThroughStripe(urlParams);
	// 	}
	// })

	// function makePaymentThroughStripe(params){
	// 	console.log(params);
	// 	console.log("call");
	// 	// $http.post(Controller.base() + 'api/stripe', params).then(function(res){
	// 	// 	console.log(res);
	// 	// })

	// 	$location.path('/review');
	// 	if (!$scope.$$phase) $scope.$apply();
	// }
}

}());