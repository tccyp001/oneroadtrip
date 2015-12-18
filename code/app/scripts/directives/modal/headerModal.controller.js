;(function() {
'use strict';

/* Controllers */

angular.module('app.controllers')
.controller('HeaderModalCtrl', [
    '$scope',
    '$modalInstance',
    '$http',
    '$cookies',
    '$cookieStore',
    '$window',
    'toastr',
    'User',
    HeaderModalCtrl
]);


function HeaderModalCtrl($scope, $modalInstance, $http, $cookies, $cookieStore, $window, toastr, User) {

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
            	console.log($scope.forms.username);
            	console.log(typeof $scope.forms.username);
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
    function callback(user) 
      {
        var userName = document.getElementById('userName');
        console.log(user.openid);
        var greetingText = document.createTextNode('Greetings, '+ user.openid + '.');
        userName.appendChild(greetingText);
      }

      //应用的APPID，请改为你自己的
      var appID = "101277978";
      //成功授权后的回调地址，请改为你自己的
      var redirectURI = "http://www.legovictor.com/#/userLogin";

      //构造请求
      console.log(window.location.hash);
      console.log(window.location.hash.length);
      if (window.location.hash.length == 6) 
      {
        var path = 'https://graph.qq.com/oauth2.0/authorize?';
        var queryParams = ['client_id=' + appID,'redirect_uri=' + redirectURI, 'scope=' + 'get_user_info,list_album,upload_pic,add_feeds,do_like','response_type=token'];
        console.log(queryParams);
        var query = queryParams.join('&');
        var url = path + query;
        // var url = 'https://graph.qq.com/oauth/show?which=ConfirmPage&display=pc&client_id=101045717&redirect_uri=http%3A%2F%2Fwww.mioji.com%2Fuser%2FsnsLogin%3Fsnstype%3Dqq&response_type=code&state=8f14e45fceea167a5a36dedd4bea2543';
        $window.open(url, 'C-Sharpcorner', 'width=500,height=400');
      }
      else 
      {
        //获取access token
        var accessToken = window.location.hash.substring(1);
        console.log(accessToken);
        //使用Access Token来获取用户的OpenID
        var path = "https://graph.qq.com/oauth2.0/me?";
        var queryParams = [accessToken, 'callback=callback'];
        var query = queryParams.join('&');
        var url = path + query;
        var script = document.createElement('script');
        script.src = url;
        document.body.appendChild(script);        
      }
    }


}

}());


