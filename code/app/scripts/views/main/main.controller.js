;(function() {
'use strict';

/* Controllers */

angular.module('app.controllers', [])
.controller('MainCtrl', [
    '$scope',
    '$http',
    MainCtrl
]);


function MainCtrl($scope, $http) {
    $scope.$parent.showfooter = true;

    $scope.tours = {
        right: [
            {
                'name': '旧金山',
                'image': 'images/city2.jpg',
                'description': '想要自由掌控行程节奏？喜欢与众不同的游玩路线？我们能满足旅行'
            },
            {
                'name': '圣地亚哥',
                'image': 'images/city3.jpg',
                'description': '想要自由掌控行程节奏？喜欢与众不同的游玩路线？我们能满足旅行'
            },

        ],

        middle: [
            {
                'name': '夏威夷',
                'image': 'images/city4.jpg',
                'description': '想要自由掌控行程节奏？喜欢与众不同的游玩路线？我们能满足旅行'
            },
            {
                'name': '拉斯维加斯',
                'image': 'images/city5.jpg',
                'description': '想要自由掌控行程节奏？喜欢与众不同的游玩路线？我们能满足旅行'
            },
        ],

        left: [
            {
                'name': '纽约',
                'image': 'images/city1.jpg',
                'description': '想要自由掌控行程节奏？喜欢与众不同的游玩路线？我们能满足旅行'
            },
        ]
    }


    $scope.features = [
        {
            'title': '线路定制',
            'icon': 'images/merit1.png',
            'content': '想要自由掌控行程节奏？喜欢与众不同的游玩路线？我们能满足旅行中的各种小任性，同时让你享受最划算的价格.'
        },
        {
            'title': '导游定制',
            'icon': 'images/merit2.png',
            'content': '想要自由掌控行程节奏？喜欢与众不同的游玩路线？我们能满足旅行中的各种小任性，同时让你享受最划算的价格.'
        },
        {
            'title': '用车定制',
            'icon': 'images/merit3.png',
            'content': '想要自由掌控行程节奏？喜欢与众不同的游玩路线？我们能满足旅行中的各种小任性，同时让你享受最划算的价格.'
        }
    ];

    $scope.corps = [
        {
            'name': '微博平台'
        },
        {
            'name': '微博平台'
        },
        {
            'name': '微博平台'
        },
        {
            'name': '微博平台'
        },
        {
            'name': '微博平台'
        },
    ]




}

}());