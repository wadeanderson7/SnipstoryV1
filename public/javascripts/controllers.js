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
	
	var ORDER_INTERVAL = 1000000;
	 
    $scope.orderProp = 'age';
    $scope.numUploading = imageHandler.numUploading;
    
    $scope.setChapter = function setChapter(idx) {
    	$scope.curChapter.lastPageIdx = $scope.pageIdx; 
    	var chapter = $scope.story.chapters[idx];
    	$scope.curChapter = chapter;
    	if (chapter.lastPageIdx !== undefined)
    		$scope.pageIdx = chapter.lastPageIdx;
    	else
    		$scope.pageIdx = 0;
    };
    
    $scope.setPageIdx = function setPageIdx(idx) {
    	$scope.pageIdx = idx;
    };
    
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
    
    $scope.addItem = function() {
    	var addSnippetModal = $modal.open({
    		templateUrl: 'addSnippetDialog.html',
    		controller: 'AddSnippetDialogCtrl',
    		backdrop: 'static'
    	});
    	
    	addSnippetModal.result.then(function (result) {   		
    		//create item
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
    
    $scope.editItem = function editItem(pageIdx, itemIdx, itemId) {
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
    };
    
    $scope.deleteItem = function deleteItem(pageIdx, itemIdx, itemId) {
    	var deleteConfirmModal = $modal.open({
    		templateUrl: 'confirmDeleteSnippet.html',
    		controller: 'ConfirmCtrl',
    	});
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
    
    $scope.addPage = function() {
    	var addPageModal = $modal.open({
    		templateUrl: 'addPageDialog.html',
    		controller: 'AddPageDialogCtrl',
    		backdrop: 'static'
    	});
    	
    	addPageModal.result.then(function (result) {   		
    		//create page
    		var chapter = $scope.curChapter;
    		var pages = chapter.pages;
    		var ordering = ORDER_INTERVAL;
    		if (pages.length > 0)
    			ordering += pages[pages.length - 1].ordering;
    		var newPage = {"description":result.description, "name":result.name, "ordering":ordering};
    		imageHandler.incUploads();
    		$http.post('/chapters/' + chapter.id + '/pages', newPage, {})
			.success(function(data, status, headers, config) {
				$scope.curChapter.pages.push(data);
				imageHandler.decUploads();
				$scope.pageIdx = pages.length - 1;
			}).error(function(data, status, headers, config) {
				//TODO: add error handler of some sort
			});
    		
    	});
    };
    
    $scope.editPage = function editPage(pageId, pageIdx) {
    	var page = $scope.curChapter.pages[pageIdx];
    	var editModal = $modal.open({
    		templateUrl: 'editPageDialog.html',
    		controller: 'EditPageDialogCtrl',
    		resolve: {
    	        name: function() { return page.name; },
    	        description: function() { return page.description; }
    	      },
    	      backdrop: 'static'
    	});
    	editModal.result.then(function(result) {
        	imageHandler.incUploads();
        	page.name = result.name;
        	page.description = result.description;
    		var data = {"name":result.name, "description":result.description};
    		$http.post('/pages/' + pageId, data)
			.success(function(data, status, headers, config) {
				imageHandler.decUploads();
			}).error(function(data, status, headers, config) {
				//TODO: add error handler of some sort
			});
    	}); 	
    };
    
    $scope.deletePage = function deletePage(pageId, pageIdx) {
    	var pages = $scope.curChapter.pages;
    	
    	if (pages.length == 1)
    		return; //can't delete last page
    	
    	var deleteConfirmModal = $modal.open({
    		templateUrl: 'confirmDeletePage.html',
    		controller: 'ConfirmCtrl',
    	});
    	deleteConfirmModal.result.then(function() {
        	imageHandler.incUploads();
        	
        	//handle changing current page index
        	
        	var curPage = pages[pageIdx];
        	if (curPage.id == pageId) {
        		if (pageIdx == pages.length-1)
        			curPage = pages[pageIdx-1];
        		else
        			curPage = pages[pageIdx+1];
        	} 
    		pages.splice(pageIdx, 1);
    		
    		//find new pageIdx
    		$scope.pageIdx = pages.indexOf(curPage);
    		
    		$http.delete('/pages/' + pageId)
			.success(function(data, status, headers, config) {
				imageHandler.decUploads();
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

snipStoryControllers.controller('ConfirmCtrl', ['$scope', '$modalInstance',
	function($scope, $modalInstance) {
	$scope.ok = function() { $modalInstance.close(); };
	$scope.cancel = function() { $modalInstance.dismiss('cancel'); };
  }]);

snipStoryControllers.controller('AddPageDialogCtrl', ['$scope', '$modalInstance',
 	function($scope, $modalInstance) {

	$scope.name = null;
 	$scope.description = null;
 	 	
 	$scope.ok = function() {
 		$modalInstance.close({description:$scope.description, name:$scope.name});
 	};
 	$scope.cancel = function() {
 		$modalInstance.dismiss('cancel');
 	};
 }]);

snipStoryControllers.controller('EditPageDialogCtrl', ['$scope', '$modalInstance', 'name', 'description',
   	function($scope, $modalInstance, name, description) {

	$scope.name = name;
   	$scope.description = description;
   	 	
   	$scope.ok = function() {
   		$modalInstance.close({description:$scope.description, name:$scope.name});
   	};
   	$scope.cancel = function() {
   		$modalInstance.dismiss('cancel');
   	};
}]);
