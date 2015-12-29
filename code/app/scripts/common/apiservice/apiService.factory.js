;(function() {
'use strict';

/**
 * Adds interceptor for auth token
 */
angular.module('app.shared')
.factory('ApiService', [
    '$resource',
    '$q',
    ApiServiceFactory,
]);

/**
 * Every function should return an instance of $resource which has custom methods implemented
 *  getAll  - getAll()
 *  get     - get({id: fooId})
 *  save    - save(fooObj)
 *  delete  - delete({id: fooId})
 *  put     - put({id: fooId}, fooObj)
 * Each method could implement a parser for incoming and outgoing.
 */
function ApiServiceFactory($resource, $q) {

    /**
     * @namespace
     */
    function ApiService() {

    }

    /**
     * return middleware for apiResource, purpose is to handle mess between backend and frontend
     * stuff like task polling for save/delete/put requests, parsing responses, etc..
     *
     * @param {Object} opts
     * @param {String[]} opts.methods
     * @param {Parser.*} opts.parser
     * @returns {Object} - interceptors used with baseResource
     */
    ApiService.prototype.middleware = function(opts) {
        var middleware = {
            getAll: {
                method: 'GET',
                interceptor: {
                    response: function(res) {

                    }
                }
            },
            get: {
                method: 'GET',
                interceptor: {
                    response: function(res) {

                    }
                }
            },
            save: {
                method: 'POST',
                interceptor: {
                    request: parser.unparse,
                    responseError: function(res) {
                        return res;
                    },
                    response: function(res) {

                    }
                }
            },
            delete: {
                method: 'DELETE',
                interceptor: {
                    responseError: function(res) {
                        return res;
                    },
                    response: function(res) {
                        return Controller.getTaskState(res);
                    }
                }
            },
            put: {
                method: 'PUT',
                interceptor: {
                    responseError: function(res) {
                        return res;
                    },
                    response: function(res) {

                    }
                }
            }
        };

        return opts.methods.reduce(function(map, method) {
            if (middleware[method]) {
                map[method] = middleware[method];
            }
            return map;
        }, {});

    };


    return new ApiService();

}

}());
