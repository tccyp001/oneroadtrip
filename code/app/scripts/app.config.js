;(function() {
'use strict';

angular.module('app')
.config([
    '$stateProvider',
    '$httpProvider',
    '$urlRouterProvider',
    '$resourceProvider',
    AppConfig
])

function AppConfig ($stateProvider, $httpProvider, $urlRouterProvider, $resourceProvider) {
    // Setting default route to display index
    $urlRouterProvider.otherwise('/main');


    $httpProvider.defaults.useXDomain = true;
    delete $httpProvider.defaults.headers.common['X-Requested-With'];

    // push interceptor
    // $httpProvider.interceptors.push('ApiInterceptor');

}


// Modal Config
angular.module('app').config(function(toastrConfig) {
  angular.extend(toastrConfig, {
    allowHtml: false,
    closeButton: false,
    closeHtml: '<button>&times;</button>',
    extendedTimeOut: 1000,
    iconClasses: {
      error: 'toast-error',
      info: 'toast-info',
      success: 'toast-success',
      warning: 'toast-warning'
    },  
    messageClass: 'toast-message',
    onHidden: null,
    onShown: null,
    onTap: null,
    progressBar: false,
    tapToDismiss: true,
    timeOut: 2000,
    titleClass: 'modal-toast-title',
    toastClass: 'modal-toast'
  });
});


}());