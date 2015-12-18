;(function() {
'use strict';

angular.module('app.shared')
.factory('Controller', [
    '$resource',
    '$http',
    ControllerFactory
]);

/**
 * ControllerFactory Definition
 */
function ControllerFactory($resource, $http) {

    function Controller(){
        // Controller properties
        // Defines the url associated with the controller
        this.persistentData = {
            scheme: 'http',
            host: '106.184.1.83',
            port: '8080',
            prefix: 'oneroadtrip6',
            version: 'v1.0'
        };

        // this.getFromLocalStorage();
    }


    /**
     * @returns {String} - Compiled url
     */
    Controller.prototype.base = function() {
        return this.persistentData.scheme + '://'
            + this.persistentData.host + ':'
            + this.persistentData.port + '/';
    };


    /**
     * @returns {String} - Simple url with only protocol + host
     */
    Controller.prototype.simpleBase = function() {
        return this.persistentData.scheme + '://'
         + this.persistentData.host;
    };


    return new Controller();
}

}());
