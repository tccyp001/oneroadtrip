;(function() {
'use strict';

angular.module('app.shared')
    .service('ApiInterceptor', [
        '$rootScope',
        ApiInterceptorFactory
    ]);

function ApiInterceptorFactory($rootScope) {
    var service = this;

    service.request = function(config) {
        // config.headers['X-Auth-Token'] = (Token.token)? Token.token.id : '';
        config.headers['Access-Control-Allow-Origin'] = '*';
        config.headers['Access-Control-Allow-Headers'] = 'Content-Type, X-Requested-With';
        config.headers['Access-Control-Allow-Methods'] = 'GET, POST, OPTIONS';        

        return config;
    };

    service.requestError = function(config) {
        return config;
    };

    service.response = function(res) {
        return res;
    };

    service.responseError = function(response) {
        var rejection;

        // switch(response.status) {
        //     case 401: {
        //         // if the error is to a definition call and user is not an admin let it slide
        //         // so it doesn't go to the catch block in the promise chain
        //         if (/(definition)/i.test(response.config.url)) {
        //             if (!Token.token.isAdmin) return response;
        //         }

        //         $rootScope.$broadcast('unauthorized');
        //         response.data = {};
        //         response.data.message = 'Invalid Login';
        //         rejection = response;
        //         break;
        //     }

        //     case 400: {
        //         if (!response) {
        //             response.data = {};
        //             response.data.message = 'Invalid Config';
        //         }
        //             rejection = response;
        //             ErrorHandling.errorMessage = response;
        //         break;
        //     }

        //     case 404: {

        //         // TODO: remove this block
        //         // 404 should not contain response.data
        //         // Even if it did, it should not log user out
        //         // Unauthorized calls should have a 401 status
        //         // if (response.data) {
        //         //     $rootScope.$broadcast('unauthorized');
        //         //     response.data = {};
        //         //     response.data.message = 'Invalid Login';
        //         // }

        //         rejection = response;
        //         break;
        //     }

        //     case 0: {
        //         response.data = {};
        //         response.data.message = 'Invalid Address';
        //         rejection = response;
        //         break;
        //     }
        // }

        return Q.reject(rejection);
    };
}

}());
