;(function(){
'use strict';

angular.module('app.directives')
.directive('travellerSelect', [
    function () {
    return {
        restrict: 'E',
        templateUrl: 'scripts/directives/travellerSelect/travellerSelect.tpl.html',
        controller: 'TravellerSelectCtrl',
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
