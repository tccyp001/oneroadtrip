;(function() {
'use strict';

angular.module('app.shared')
    .factory('AccessToken', [
    '$resource',
    '$http',
    '$window',
    'User',
    AccessTokenFactory
]);


function AccessTokenFactory($resource, $http, $window, User) {
	function OauthToken(){
		this.accessToken = {};
		this.openID = {};
		this.userInfo = {};
		this.status = '';
		this.type = '';
	}

	OauthToken.prototype.setTokenFromString = function(str){
		// var str='access_token=CB1551A90B3308CAB76603B47625C516&expires_in=7776000';
		var lists = str.split('&');
		var that = this;

		_.each(lists, function(list) {
		    that.accessToken[list.split('=')[0]] = list.split('=')[1];
		})
		
		if(this.accessToken && this.accessToken.state === 'weibo') {
			this.getOpenIDForWeibo();
		} else if(this.accessToken && this.accessToken.state === 'qq') {
			this.getOpenIDForQQ();
		}
		
	};


	OauthToken.prototype.getOpenIDForQQ = function(){
		var that = this;
		var url = 'https://graph.qq.com/oauth2.0/me' + '?callback=callback' + '&access_token=' + this.accessToken.access_token;
		console.log(url);	
		$window.callback = function(data) {
			
			that.openID = data;
			var auth = {
				'type': 'QQ_OAUTH',
				'access_token': that.accessToken.access_token,
				'client_id': that.openID.client_id,
				'open_id': that.openID.openid
			}
			User.signup(auth).then(function(){
				that.status = 'done';
			});
		}

		$http.jsonp(url).then(function(res){
			console.log('done');
		})

	}


	OauthToken.prototype.getOpenIDForWeibo = function(){
		console.log(this);
		var that = this;
		var auth = {
			'type': 'WEIBO_OAUTH',
			'access_token': this.accessToken.access_token,
			'uid': this.accessToken.uid,
		}
		User.signup(auth).then(function(){
			that.status = 'done';
		});
		// User.login();
	}

	OauthToken.prototype.getUserInfo = function(){
		var url = 'https://graph.qq.com/user/get_user_info?access_token=' + this.accessToken.access_token + '&oauth_consumer_key=' + this.openID.client_id + '&openid=' + this.openID.openid;
	
			$http.get(url).then(function(res){
				this.status = "done";
		})
	}

	return new OauthToken();

}

}());