;(function() {
'use strict';

angular.module('app.shared', [])
    .factory('User', [
        '$rootScope',
        '$resource',
        '$state',
        '$cookies',
        'Controller',
        UserFactory
    ]);

function UserFactory($rootScope, $resource, $state, $cookies, Controller) {

    /**
     * Creates a new User
     * @constructor
     */
    function User() {
        // only these fields will be persisted
        this.persistentData = {
            token: '',
            loggedIn: false,
            username: '',
            email:''
        };

        // // non persisted properties
        // this.currNode = undefined;
        // this.displayNodeLevels = [];

        // // load from local storage on init
        // this.getFromLocalStorage();
        // * set the auth token only use for {@link ApiService} 
        // Token.token = this.persistentData.token;

        // // expire timer starts now
        // var timeout = this.persistentData.token.expires - Date.now();
        // this.expireTimer = timer(timeout, this.logout.bind(this));
    }

    User.prototype.signup = function(auth) {
        return $resource(Controller.base() + 'api/signup')
            .save(auth).$promise
            // .then(function(res) {

            //     // var parsedToken = parseUserToken(res);
            //     // this.persistentData.token = parsedToken;
            //     // this.persistentData.loggedIn = true;
            //     // this.persistentData.username = auth.UserName;

            //     // // save new persistentData
            //     // this.setToLocalStorage();
            //     // Token.token = this.persistentData.token;

            //     // // reset expire timer to new expire time
            //     // clearTimeout(this.expireTimer);
            //     // var timeout = this.persistentData.token.expires - Date.now();
            //     // this.expireTimer = timer(timeout, this.logout.bind(this));
            // });
    };

    /**
     * Log the user in, handle state of persistent token
     * Makes an post call to /auth_credentials with credentials, on success set the persistentData, and
     * starts a new expire timer
     *
     * @param {Object} auth
     * @param {String} auth.Username
     * @param {String} auth.Password
     */
    User.prototype.login = function(auth) {
        return $resource(Controller.base() + 'api/login')
            .save(auth).$promise
            // .then(function(res) {
            //     console.log(res)
            //     // var parsedToken = parseUserToken(res);
            //     // this.persistentData.token = parsedToken;
            //     // this.persistentData.loggedIn = true;
            //     // this.persistentData.username = auth.UserName;

            //     // // save new persistentData
            //     // this.setToLocalStorage();
            //     // Token.token = this.persistentData.token;

            //     // // reset expire timer to new expire time
            //     // clearTimeout(this.expireTimer);
            //     // var timeout = this.persistentData.token.expires - Date.now();
            //     // this.expireTimer = timer(timeout, this.logout.bind(this));
            // });
    };


    /**
     * Log the user in by token
     * Makes an post call to /auth_token with credentials, on success set the persistentData, and
     * starts a new expire timer
     *
     * @param {String} token
     * @returns {Promise} - User logged in, connect data manager and sync resources
     */
    User.prototype.loginbytoken = function(token) {
        return $resource(Controller.base() + '/auth_token')
            .save(token).$promise
            .then(function(res) {
                var parsedToken = parseUserToken(res);
                this.persistentData.token = parsedToken;
                this.persistentData.loggedIn = true;
                this.persistentData.username = "user";

                // save new persistentData
                this.setToLocalStorage();
                Token.token = this.persistentData.token;

                // reset expire timer to new expire time
                clearTimeout(this.expireTimer);
                var timeout = this.persistentData.token.expires - Date.now();
                this.expireTimer = timer(timeout, this.logout.bind(this));
            }.bind(this))
            .then(DataManager.connect.bind(DataManager))
            .then(Resources.sync.bind(Resources))
            .then(this.syncWithResources.bind(this));
    };


    /**
     * Logout the user, clears all the persisted data
     * Prune the resource graph, and disconnect data manager socket connection
     * clears the expire time
     */
    User.prototype.logout = function() {
        var cookies = $cookies.getAll();
            _.each(cookies, function (v, k) {
                $cookies.remove(k);
        });

        // clearTimeout(this.expireTimer);

        if (!$rootScope.$$phase) $rootScope.$apply();
    }

    var user = new User();

    // Log user out on unauthorized response
    $rootScope.$on('unauthorized', function() {
        user.logout();
    });

    return user;

}



/**
 * Set timeout wrapper
 * @param {Number} timeout - how long later to call function
 * @param {Function} fn - function to call at a later time
 * @returns {setTimeout} - reference to clearTimeout if needed
 */
function timer(timeout, fn) {
    return setTimeout(fn, timeout);
}

/**
 * Parse backend response for Token /auth
 * @param {JSON} res - backend response
 * @returns {Object} - parsed user token
 */
function parseUserToken(res){
    var parseTimeFormat = new Date(res.Token.expiresAt).getTime();
    return {
        expires: parseTimeFormat,
        id: res.Token.tokenId,
        isAdmin: res.Token.isAdmin
    };
}

}());
