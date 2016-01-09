;(function() {
'use strict';

angular.module('app')
.run([
	'$http',
    '$rootScope',
    '$state',
    '$location',
    AppRun
]);

function AppRun ($http, $rootScope, $state, $location) {

      // var appID = "3268335867";
      // //成功授权后的回调地址，请改为你自己的
      // var redirectURI = "http://www.oneroadtrip.com";

    // Handle the Oauth
    $rootScope.$on('$locationChangeStart', function(event, location) {
      //   if (location.search('code=') > 0) {
      //   	var code = location.split('?')[1].split('#')[0].split('=')[1];
      //   	console.log(code);
      // // var path = 'https://api.weibo.com/oauth2/access_token?';
      // // var queryParams = ['client_id=' + appID, 'client_secret=' + appSecret, 'code=' + code,'redirect_uri=' + redirectURI,'grant_type=authorization_code'];
      // // var query = queryParams.join('&');
      // // var url = path + query;
      // var url = "https://api.weibo.com/oauth2/access_token?client_id=" + appID + "&client_secret=" + appSecret + "&grant_type=authorization_code&code=" + code + "&redirect_uri=" + redirectURI;

      //   	$http.post(url).then(function(res){
      //   		console.log(res);
      //   	})


	     //  // var path = 'https://api.weibo.com/oauth2/access_token?';
	     //  // var queryParams = ['client_id=' + appID, 'client_secret=' + appSecret, 'redirect_uri=' + redirectURI,'code=' + code, 'grant_type=authorization_code'];
	     //  // var query = queryParams.join('&');
	     //  // var url = path + query;
      //  //    window.location = url;
      //   };

    })
}


}());
