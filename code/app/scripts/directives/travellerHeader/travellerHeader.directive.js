;(function(){
'use strict';

angular.module('app.directives', [])
.directive('travellerHeader', [
    function () {
    return {
        restrict: 'E',
        templateUrl: 'scripts/directives/travellerHeader/travellerHeader.tpl.html',
        controller: 'HeaderCtrl',
        link: linkFunc
    };
}]);

function linkFunc (scope, elem, attrs) {
	var element = angular.element($('.dropdown-flag'))

	element.mouseenter(function(e){
		angular.element($('.header-dropdown-manu')).css('display', 'block');
	})
	element.mouseleave(function(){
		angular.element($('.header-dropdown-manu')).css('display', 'none');
	})
}


})();
