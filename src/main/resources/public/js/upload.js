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
        this.uploadFileToUrl = function(file, uploadUrl, scope){
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
                    if (data.result.length === 3 && data.result[2] === "Audit done."
                    && data.result[1] === "Starting audit...")
                        scope.accepted = true;
                })

                .error(function(data){
                    console.log('Failed to upload file');
                    console.log(data);
                    scope.loading = false;
                });
        }
    }]);

    app.controller('uploadCtrl', ['$scope', 'fileUpload', function($scope, fileUpload){
        $scope.result = [ ];
        $scope.loading = false;
        $scope.accepted = false;

        $scope.uploadFile = function(){
            $scope.accepted = false;
            var file = $scope.javaFile;
            $scope.loading = true;
            var uploadUrl = "/upload";
            fileUpload.uploadFileToUrl(file, uploadUrl, $scope);
        };

    }]);

})();

