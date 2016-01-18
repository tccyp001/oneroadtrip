;(function() {
'use strict';

angular.module('app.views', ['ui.router'])
.config([
    '$stateProvider',
    DashboardConfig
]);

function DashboardConfig ($stateProvider) {

	$stateProvider.state('main', {
        url: '/main',
		templateUrl: 'scripts/views/main/main.tpl.html',
        controller: 'MainCtrl'
    });

    $stateProvider.state('tour', {
        url: '/tour',
        templateUrl: 'scripts/views/tour/tour.tpl.html',
        controller: 'TourCtrl'
    });

    $stateProvider.state('userLogin', {
        url: '/userLogin',
        templateUrl: 'scripts/views/user/userLogin.tpl.html',
        controller: 'UserLoginCtrl'
    });

    $stateProvider.state('guide', {
        url: '/guide',
        templateUrl: 'scripts/views/guide/guide.tpl.html',
        controller: 'GuideCtrl'
    });

    $stateProvider.state('review', {
        url: '/review',
        templateUrl: 'scripts/views/review/review.tpl.html',
        controller: 'ReviewCtrl'
    });

    $stateProvider.state('confirm', {
        url: '/confirm',
        templateUrl: 'scripts/views/confirm/confirm.tpl.html',
        controller: 'ConfirmCtrl'
    });

    $stateProvider.state('accessToken', {
        url: '/access_token=:accessToken',
        templateUrl: 'scripts/directives/modal/oauthModal.tpl.html',
        controller: 'OauthModalCtrl'
    });

    $stateProvider.state('resetpwd', {
        url: '/reset_pwd=:accessToken',
        templateUrl: 'scripts/views/resetpwd/resetpwd.tpl.html',
        controller: 'ResetPwdCtrl'
    });


}

}());
