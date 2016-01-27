(function(){
    var app = angular.module('upload', []);

    app.directive('fileModel', ['$parse', function ($parse) {
        return {
            restrict: 'A',
            link: function(scope, element, attrs) {
                var model = $parse(attrs.fileModel);
                var modelSetter = model.assign;

                element.bind('change', function(){
                    scope.$apply(function(){
                        modelSetter(scope, element[0].files[0]);
                    });
                });
            }
        };
    }]);

    app.service('fileUpload', ['$http', function ($http) {
        this.uploadFileToUrl = function(file, uploadUrl, scope, accepted){
            var fd = new FormData();
            fd.append('file', file);

            $http.post(uploadUrl, fd, {
                    transformRequest: angular.identity,
                    headers: {'Content-Type': undefined}
                })

                .success(function(data){
                    console.log('File uploaded !');
                    console.log(data);
                    scope.result = data.result;
                    scope.loading = false;
                    if (accepted(data.result))
                        scope.accepted = true;
                })

                .error(function(data){
                    console.log('Failed to upload file');
                    console.log(data);
                    scope.loading = false;
                });
        }
    }]);

    app.directive('styleCheck', function() {
        return {
            restrict: 'E',
            templateUrl: 'templates/check-style.html',
            controller: ['$scope', 'fileUpload', function ($scope, fileUpload) {
                $scope.result = [];
                $scope.loading = false;
                $scope.accepted = false;

                $scope.uploadFile = function () {
                    $scope.accepted = false;
                    var file = $scope.javaFile;
                    $scope.loading = true;
                    var uploadUrl = "/upload";
                    fileUpload.uploadFileToUrl(file, uploadUrl, $scope, function(result) {
                        return result.length === 3 && result[2] === "Audit done."
                        && result[1] === "Starting audit...";
                    });
                };

            }],
            controllerAs: 'upCtrl'
        };
    });

    app.controller('panelController', ['$scope',function($scope){
        $scope.selected = 1;
        $scope.select = function(newVal) {
            this.selected = newVal;
        };
        $scope.isSelect = function(selected) {
            return $scope.selected === selected;
        };

    }]);

    app.directive('findBugs', function () {
        return {
            restrict: 'E',
            templateUrl: 'templates/find-bugs.html',
            scope : {},
            controller:  ['$scope', 'fileUpload', function ($scope, fileUpload) {
            $scope.result = [];
            $scope.loading = false;
            $scope.accepted = false;

            $scope.uploadFile = function () {
                console.log("fb - start upload");
                $scope.accepted = false;
                var file = $scope.javaFile;
                $scope.loading = true;
                var uploadUrl = "/checkForBugs";
                fileUpload.uploadFileToUrl(file, uploadUrl, $scope, function(result){
                    return (result.length == 1 && result[0].indexOf("Running findbugs on ") > -1);
                });
            };

        }],
            controllerAs: 'upCtrl'
        };
    });

    app.directive('help', function() {
        return {
            restrict: 'E',
            templateUrl: 'templates/help.html'
        };
    });
})();

