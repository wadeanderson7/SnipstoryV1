var snipStoryControllers = angular.module('snipStoryControllers', []);

snipStoryControllers.controller('EditorCtrl', ['$scope', '$http',
  function ($scope, $http) {
	$http.get('/mystory/all').success(function(data) {
	  $scope.story = data;
	  $scope.curChapter = data.chapters[0];
	  $scope.pageId = data.chapters[0].pages[0].id;
	});
	 
    $scope.orderProp = 'age';
    
    $scope.getPicUrl = function getPicUrl(picture, thumbType) {
    	var url = picture.url;
    	var index = url.indexOf(picture.id) + picture.id.length + 1;
    	return url.substring(0,index) + "thumb_" + thumbType + "." + url.substring(index, url.length);
    };
    
}]);