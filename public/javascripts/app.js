var snipStoryApp = angular.module('snipStoryApp', [
	'ngRoute',
	'snipStoryControllers'
]);

snipStoryApp.config(['$routeProvider',
	function($routeProvider) {
		$routeProvider.
			when('/editor',  {
				templateUrl: 'assets/partials/editor.html',
				controller: 'EditorCtrl',
			}).
			when('/toc',  {
				templateUrl: 'assets/partials/toc.html',
				controller: 'TocCtrl',
			}).
			otherwise({
				redirectTo: '/editor'
			});
}]);
