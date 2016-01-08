;(function(){
'use strict';

angular.module('app.filters', [])
.filter('trusted', [
	'$sce', 
	TrustedFilter
]);


function TrustedFilter ($sce) {
	return function(url) {
        return $sce.trustAsResourceUrl(url);
    };
}

})();
