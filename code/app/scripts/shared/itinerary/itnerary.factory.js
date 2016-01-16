;(function() {
'use strict';

angular.module('app.shared')
    .factory('Itnerary', [
        '$http',
        'VisitCity',
        'Edge',
        'GuideInfo',
        'Quote',
        'Order',
        ItneraryFactory
    ]);


function ItneraryFactory($http, VisitCity, Edge, GuideInfo, Quote, Order) {

    function Itnerary(opt) {
        // this.user_token = '';
        // this.startdate = '';

        // this.city = VisitCity;
        // this.edge = Edge;
        // this.guide_for_whole_trip = GuideInfo;

        // this.num_people = '';
        // this.num_room = '';
        // this.hotel = '';
        // this.quote_for_one_guide = Quote;
        // this.quote_for_multiple_guides = Quote;

        // this.choose_one_guide_solution = false;
        // this.itinerary_id = '';
        // this.reservation_id = '';
        // this.order = Order;
    }



    return new Itnerary();
}

}());
