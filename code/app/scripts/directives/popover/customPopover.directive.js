;(function(){
'use strict';

angular.module('app.directives')
.directive('customPopover', function ($compile) {
        return {
            restrict: "A",
            template: "<span class='popover'></span>",
            link: function (scope, element, attrs) {
                var popOverContent;
                var options = {
                    trigger: 'hover',
                    content: scope.content,
                    placement: "top",
                    html: true
                };
                $(element).popover(options);
            },
            scope: {
                title: '@',
                content: '@'
            }
        };
    });


})();
