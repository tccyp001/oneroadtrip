;(function() {
'use strict';

/* Controllers */

angular.module('app.controllers')
.controller('ReviewCtrl', [
    '$scope',
    '$http',
    '$location',
    '$timeout',
    'TourInfo',
    'Controller',
    ReviewCtrl
]);


function ReviewCtrl($scope, $http, $location, $timeout, TourInfo, Controller) {

	$scope.quote = TourInfo.quote;


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