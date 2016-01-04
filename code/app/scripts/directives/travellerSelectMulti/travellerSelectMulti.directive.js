;(function(){
'use strict';

angular.module('app.directives')
.directive('travellerSelectMulti', ['$timeout', 'toastr',
    function ($timeout, toastr) {
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
        link: linkFunc.bind(null, $timeout, toastr)
    };
}]);


function linkFunc($timeout, toastr, scope, elem, attrs) {

    var searchInput = elem.find('.controllerInput');
    searchInput.on('focusin', function(e){
        scope.showlayer = true;
        $timeout(function(){}, 0);
    });


    searchInput.on('keyup', _.debounce(searchHandler, 200));

    scope.$watch('options', function(val){
        if (val) {
            scope.options_real = _.clone(val);
        };
    })


    function searchHandler(e) {
        var term = e.target.value;
        var regexp = new RegExp(term, 'i');

        scope.options_real = _(scope.options)
        .map(function(city) {
            if (regexp.test(city.name)) {
                return city;
            } else {
                for(var i = 0; i < city.alias.length; i++) {
                    if (regexp.test(city.alias[i])) {
                        return city;
                    }
                }
            }
        })
        .compact()
        .value();

        $timeout(function(){}, 0);
    }



    // $scope.$watchCollection('selected_city', function(val){
    //     if (val && val.length === 0) {
    //         $scope.selected_show = true;
    //     }
    // })

    scope.selected_city_ids = [];
    scope.selected_city = [];
    scope.selected_show = true;

    scope.addSelectCity = function(option){
        scope.selected_show = false;
        var obj = {"city": {"city_id" : option.value}};
        var index = _.findIndex(scope.selected_city_ids, function(city) {
            return city.city.city_id === option.value;
        })
        if (index === -1 && option.value) {
            scope.selected_city_ids.push(obj);
            scope.selected_city.push(option);
            scope.showlayer = false;
            searchInput[0].value = '';             
        } else {
            toastr.error("城市已经添加");
        }

        scope.selected = scope.selected_city_ids;
        // var index = scope.options_real.indexOf(option);
        // scope.options_copy.splice(index, 1);
        // scope.showlayer = false;
    }

    scope.removeSelectedCity = function(city){
        var index = scope.selected_city.indexOf(city);
        scope.selected_city.splice(index, 1);
        var index_id = scope.selected_city_ids.indexOf(city);
        scope.selected_city_ids.splice(index, 1);
    }



    scope.selected = undefined;


    scope.showlayer = false;

    scope.show = function(){
        scope.showlayer = !scope.showlayer;
    }


    // scope.selectCity = function(option){
    //     scope.selected_city_name = option.name;
    //     scope.selected = option.value;
    //     scope.showlayer = false;
    // }

}



})();
