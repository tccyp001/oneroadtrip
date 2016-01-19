;(function() {
'use strict';

angular.module('app.shared')
    .factory('OrderInfo', [
        '$http',
        OrderInfoFactory
    ]);


function OrderInfoFactory($http) {

    function OrderInfo(opt) {


    }

    return new OrderInfo();
}

}());
