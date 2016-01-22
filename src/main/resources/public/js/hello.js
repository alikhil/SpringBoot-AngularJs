/**
 * Created by user on 1/21/16.
 */
angular.module('hello', []).controller('home', function($scope, $http) {
    $http.get('resource/').success(function(data) {
        $scope.greeting = data;
    })
});