;(function() {
'use strict';

angular.module('app.shared')
    .factory('QuoteInfo', [
        '$http',
        QuoteInfoFactory
    ]);


function QuoteInfoFactory($http) {

    function QuoteInfo(opt) {


    }

    return new QuoteInfo();
}

}());
