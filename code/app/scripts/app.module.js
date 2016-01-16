;(function() {
'use strict';

angular.module('gulp.templateCache', []);

angular.module('app', [
    'app.views',
    'app.directives',
    'app.shared',
    'app.controllers',
    'app.filters',
    'views.users',
    'ui.router',
    'ui.bootstrap',
    'ngResource',
    'ngMaterial',
    'ngCookies',
    'toastr',
    'stripe.checkout',
    'ngMap',
    'dndLists',
    'daterangepicker',
]);


}());
