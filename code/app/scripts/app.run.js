;(function() {
'use strict';

angular.module('app')
.run([
    '$rootScope',
    '$state',
    '$location',
    AppRun
]);

function AppRun ($rootScope, $state, $location) {

    // Handle the Oauth
    $rootScope.$on('$locationChangeStart', function() {
        console.log($location.url());

    })
}


}());
