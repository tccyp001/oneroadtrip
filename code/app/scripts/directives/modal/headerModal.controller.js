;(function() {
'use strict';

/* Controllers */

angular.module('app.controllers')
.controller('HeaderModalCtrl', [
    '$scope',
    '$modal',
    '$modalInstance',
    '$http',
    '$cookies',
    '$cookieStore',
    '$window',
    'toastr',
    'User',
    'AccessToken',
    HeaderModalCtrl
]);


function HeaderModalCtrl($scope, $modal, $modalInstance, $http, $cookies, $cookieStore, $window, toastr, User, AccessToken) {

	$scope.forms = {};

	$scope.closeModal = function(){
    	$modalInstance.close();
  	}

	$scope.changeStatus = function(status) {
		$scope.userStatus = status;
	}

	$scope.signup = function() {
		var forms = _.clone($scope.forms);
		User.signup(forms)
	    .then(function(res) {
	     if (res.status === 'SUCCESS') {
				$cookieStore.put('username', $scope.forms.username);
				$cookieStore.put('token', res.token);
				$cookieStore.put('isLoggin', true);
				$modalInstance.close();
				$scope.$parent.updateHeader();
	          	toastr.success('Signup Success', '');    
	        } else {
	          toastr.error(res.status);  
	        }
	    })
	    .catch(function(e) {
          toastr.error(e.toString());
	    })
	}


  $scope.login = function() {
    var forms = _.clone($scope.forms);
      	User.login(forms)
        .then(function(res) {
            if (res.status === 'SUCCESS') {
        				$cookieStore.put('username', $scope.forms.username);
        				$cookieStore.put('token', res.token);
        				$cookieStore.put('isLoggin', true);
              	$modalInstance.close();
              	$scope.$parent.updateHeader();
	          	toastr.success('Login Success');    
            } else {
              toastr.error(res.status);  
            }
        })
        .catch(function(e) {
          toastr.error(e.toString());
        })
  }


  $scope.oauthThroughQQ = function(){
      //应用的APPID，请改为你自己的
      var appID = "101277978";
      //成功授权后的回调地址，请改为你自己的
      var redirectURI = "http://www.oneroadtrip.com";

      //构造请求
        var path = 'https://graph.qq.com/oauth2.0/authorize?';
        var queryParams = ['client_id=' + appID,'redirect_uri=' + redirectURI, 'scope=' + 'get_user_info,list_album,upload_pic,add_feeds,do_like','response_type=token', 'state=qq'];
        var query = queryParams.join('&');
        var url = path + query;
        $window.open(url, 'C-Sharpcorner', 'width=500,height=400');

        $modalInstance.close();
    }


  $scope.oauthThroughWeibo = function(){
      //应用的APPID，请改为你自己的
      var appID = "3268335867";
      //成功授权后的回调地址，请改为你自己的
      var redirectURI = "http://www.oneroadtrip.com";
      //构造请求

      var path = 'https://api.weibo.com/oauth2/authorize?';
      var queryParams = ['client_id=' + appID,'redirect_uri=' + redirectURI, 'response_type=token', 'forcelogin=false', 'state=weibo'];
      var query = queryParams.join('&');
      var url = path + query;
      $window.open(url, 'C-Sharpcorner', 'width=500,height=400');
      $modalInstance.close();
    }


}

}());


