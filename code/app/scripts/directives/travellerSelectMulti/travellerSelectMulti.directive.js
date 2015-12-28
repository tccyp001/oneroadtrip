;(function(){
'use strict';

angular.module('app.directives')
.directive('travellerSelectMulti', [
    function () {
    return {
        restrict: 'E',
        templateUrl: 'scripts/directives/travellerSelectMulti/travellerSelectMulti.tpl.html',
        controller: 'travellerSelectMultiCtrl',
        scope: {
        	options: '=',
        	selected: '=',
        	placeholder: '@',
            icon:'@'
        },
        link: function (scope, iElement, iAttrs) {
        }
    };
}]);

})();
