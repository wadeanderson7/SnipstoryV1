var snipStoryControllers = angular.module('snipStoryControllers', ['ui.bootstrap', 'snipStoryServices']);

snipStoryControllers.controller('EditorCtrl', ['$scope', '$http', '$modal', 'imageHandler',
  function ($scope, $http, $modal, imageHandler) {
	$http.get('/mystory/all').success(function(data) {
	  $scope.story = data;
	  $scope.curChapter = data.chapters[0];
	  $scope.pageIdx = 0; //TODO: update with scroll
	});
	
	window.onbeforeunload =  function() {
		if (imageHandler.syncInProgress())
			return "Syncing is not complete. Leaving the page now will cause errors.";
		else
			return null;
	};
	 
    $scope.orderProp = 'age';
    $scope.numUploading = imageHandler.numUploading;
    
    $scope.getPicUrl = function getPicUrl(picture, thumbType) {
    	var url = picture.url;
    	var index = url.indexOf(picture.id) + picture.id.length + 1;
    	if (thumbType == "original")
    		return url;
    	else {
    		//return url.substring(0,index) + "thumb_" + thumbType + "." + url.substring(index, url.length);
    		return url.substring(0,index) + "thumb_" + thumbType + ".jpeg"; //all thumbnails are jpegs for now
    	}
    };
    
    $scope.getLocalPic = function getLocalPic(imageId, thumbType) {
    	return imageHandler.pics[imageId.toString()][thumbType];
    };
    
    $scope.deleteItem = function deleteItem(itemId, itemIdx) {
    	var deleteConfirmModal = $modal.open({
    		templateUrl: 'confirmDeleteSnippet.html',
    		controller: 'ConfirmDeleteSnippetCtrl',
    	});
    	var pageIdx = $scope.pageIdx;
    	deleteConfirmModal.result.then(function() {
        	imageHandler.incUploads();
    		$scope.curChapter.pages[pageIdx].items.splice(itemIdx, 1);
    		$http.delete('/items/' + itemId)
			.success(function(data, status, headers, config) {
				imageHandler.decUploads();
			}).error(function(data, status, headers, config) {
				//TODO: add error handler of some sort
			});
    	});
    };
    
    $scope.editItem = function editItem(itemId, itemIdx) {
    	var pageIdx = $scope.pageIdx;
    	var item = $scope.curChapter.pages[pageIdx].items[itemIdx];
    	var editConfirmModal = $modal.open({
    		templateUrl: 'editSnippetDialog.html',
    		controller: 'EditSnippetCtrl',
    		resolve: {
    	        thumbnail: function () {
    	          if (item.localImageId)
    	        	  return $scope.getLocalPic(item.localImageId, 'medium');
    	          else if (item.picture)
    	        	  return $scope.getPicUrl(item.picture, 'medium');
    	          else
    	        	  return null;
    	        },
    	        caption: function() { return item.description; }
    	      },
    	      backdrop: 'static'
    	});
    	editConfirmModal.result.then(function(result) {
        	imageHandler.incUploads();
        	item.description = result.caption;
        	if (result.picId) {
        		item.localImageId = result.picId;
        		item.picture = null;
        		
        		if (!imageHandler.assignWaitingPicToItem(item.id)) {
    				imageHandler.addItemWaitingForPic(result.picId, item.id);
    			}
        	}
    		var data = {"description":result.caption};
    		$http.post('/items/' + itemId, data)
			.success(function(data, status, headers, config) {
				imageHandler.decUploads();
			}).error(function(data, status, headers, config) {
				//TODO: add error handler of some sort
			});
    	}); 	
    }
    
    $scope.showAddSnippetDialog = function() {
    	
    	var addSnippetModal = $modal.open({
    		templateUrl: 'addSnippetDialog.html',
    		controller: 'AddSnippetDialogCtrl',
    		backdrop: 'static'
    	});
    	
    	addSnippetModal.result.then(function (result) {   		
    		//create item
    		var ORDER_INTERVAL = 1000000;
    		var pageIdx = $scope.pageIdx;
    		var page = $scope.curChapter.pages[pageIdx];
    		var items = page.items;
    		var ordering = ORDER_INTERVAL;
    		if (items.length > 0)
    			ordering += items[items.length - 1].ordering;
    		var newItem = {"description":result.caption, "type":"IMAGE", "ordering":ordering};
    		$http.post('/pages/' + page.id + '/item', newItem, {})
			.success(function(data, status, headers, config) {
				data.localImageId = result.picId;
				$scope.curChapter.pages[pageIdx].items.push(data);
				//add waiting image to item or add item to list of items waiting for pics to upload
				if (!imageHandler.assignWaitingPicToItem(data.id)) {
					imageHandler.addItemWaitingForPic(result.picId, data.id);
				}
				
			}).error(function(data, status, headers, config) {
				//TODO: add error handler of some sort
			});
    		
    	});
    	
    };
    
}]);

snipStoryControllers.controller('AddSnippetDialogCtrl', ['$scope', '$modalInstance', 'imageHandler',
	function($scope, $modalInstance, imageHandler) {

	$scope.thumbnail = null;
	$scope.picId = null;
	$scope.caption = null;
	
	$scope.selectPic = function() {
		$("#picFileInput").click();
	};
	
	$scope.uploadPic = function uploadPic(picFile) {
		var picId = imageHandler.doThumbnailAndUpload(picFile, function() {
			$scope.thumbnail = imageHandler.pics[picId.toString()].original;
			$scope.$apply();
		});
		$scope.picId = picId;
	};
	
	$scope.ok = function() {
		$modalInstance.close({picId:$scope.picId, caption:$scope.caption});
	};
	$scope.cancel = function() {
		//TODO: if picture is uploaded, delete it - if it is queued for upload, queue if for deletion
		$modalInstance.dismiss('cancel');
	};
}]);

snipStoryControllers.controller('EditSnippetCtrl', ['$scope', '$modalInstance', 'imageHandler', 'thumbnail', 'caption',
 	function($scope, $modalInstance, imageHandler, thumbnail, caption) {

 	$scope.thumbnail = thumbnail;
 	$scope.picId = null;
 	$scope.caption = caption;
 	
 	$scope.selectPic = function() {
 		$("#picChangeFileInput").click();
 	};
 	
 	$scope.uploadPic = function uploadPic(picFile) {
 		var picId = imageHandler.doThumbnailAndUpload(picFile, function() {
 			$scope.thumbnail = imageHandler.pics[picId.toString()].original;
 			$scope.$apply();
 		});
 		$scope.picId = picId;
 	};
 	
 	$scope.ok = function() {
 		$modalInstance.close({picId:$scope.picId, caption:$scope.caption});
 	};
 	$scope.cancel = function() {
 		//TODO: if picture is uploaded, delete it - if it is queued for upload, queue if for deletion
 		$modalInstance.dismiss('cancel');
 	};
 }]);

snipStoryControllers.controller('ConfirmDeleteSnippetCtrl', ['$scope', '$modalInstance',
 	function($scope, $modalInstance, imageHandler) {
 	
 	$scope.ok = function() {
 		$modalInstance.close();
 	};
 	$scope.cancel = function() {
 		$modalInstance.dismiss('cancel');
 	};
 }]);