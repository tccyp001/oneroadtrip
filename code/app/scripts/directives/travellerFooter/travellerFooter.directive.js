;(function(){
'use strict';

angular.module('app.directives')
.directive('travellerFooter', [
    function () {
    return {
        restrict: 'E',
        templateUrl: 'scripts/directives/travellerFooter/travellerFooter.tpl.html',
        controller: 'FooterCtrl',
        link: function (scope, iElement, iAttrs) {
        }
    };
}]);

})();
