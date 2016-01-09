;(function() {
'use strict';

angular.module('app.shared')
    .factory('AccessToken', [
    '$resource',
    '$http',
    '$window',
    AccessTokenFactory
]);


function AccessTokenFactory($resource, $http, $window) {
	function OauthToken(){
		this.accessToken = {};
		this.openID = {};
		this.userInfo = {};
		this.status= '';
	}

	OauthToken.prototype.setTokenFromString = function(str){
		// var str='access_token=CB1551A90B3308CAB76603B47625C516&expires_in=7776000';
		var lists = str.split('&');
		var that = this;

		_.each(lists, function(list) {
		    that.accessToken[list.split('=')[0]] = list.split('=')[1];
		})
		
		this.getOpenID();
	};


	OauthToken.prototype.getOpenID = function(){
		var that = this;
		var url = 'https://graph.qq.com/oauth2.0/me' + '?callback=callback' + '&access_token=' + this.accessToken.access_token;
		console.log(url);	
		$window.callback = function(data) {
			console.log(data);
			that.openID = data;
			that.getUserInfo();
		}

		$http.jsonp(url).then(function(res){
			console.log('done');
		})


		console.log(this);
	}

	OauthToken.prototype.getUserInfo = function(){
		var url = 'https://graph.qq.com/user/get_user_info?access_token=' + this.accessToken.access_token + '&oauth_consumer_key=' + this.openID.client_id + '&openid=' + this.openID.openid;
	
			$http.get(url).then(function(res){
				console.log(res);
				this.status = "done";
		})
	}

	return new OauthToken();

}

}());