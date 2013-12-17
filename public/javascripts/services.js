var snipStoryServices = angular.module('snipStoryServices', []);

snipStoryServices.factory('storyService', ['$http',
    function ($http) {
	
	var story = null;
	
	return {
		getStory : function getStory(callback) {
			if (story == null) {
				$http.get('/mystory/all').success(function(data) {
					  story = data;
					  callback(story);
					});
			} else {
				callback(story);
			}
		}
	};
}]);

snipStoryServices.factory('imageHandler', ['$http', '$document',
    function ($http, $document) {
		var canvas = document.createElement('canvas');
		var context = canvas.getContext('2d');
		canvas.width = 1500;
		
		var remotePictures = {};
		var uuidWaitingForItem = {value:null};
		var itemsWaitingForUuids = {}; 
		var pics = {};
		var nextId = 1;
		var numUploading = {value:0};
		
		function incUploads() {
			numUploading.value++;
		};
		function decUploads() {
			numUploading.value--;
		};
		function syncInProgress() {
			return numUploading.value > 0;
		}
		function getNextId() {
			return nextId++;
		}
		
		return {
			pics : pics,
			assignWaitingPicToItem : assignWaitingPicToItem,
			syncInProgress : syncInProgress,
			numUploading : function() { return numUploading.value; },
			addItemWaitingForPic : function addItemWaitingForPic(picId, itemId) { itemsWaitingForUuids[picId] = itemId; },
			incUploads : incUploads,
			decUploads : decUploads,
			doThumbnailAndUpload : function(file, callback, uploadDoneCallback) {
				if (!file.type.match('image.*')) {
			        return null;
			    }
				
				var reader = new FileReader();
				var id = getNextId();
				pics[id.toString()] = {original: null, flow: null};
			    reader.onload = function(e) {
		    		loadImage(e.target.result, id, callback, uploadDoneCallback);
		    	};
		    	reader.readAsDataURL(file);
		    	return id;
			}
		};
		
		function loadImage(file, id, callback, uploadDoneCallback) {
			incUploads();
			var img = new Image();
			img.src = file;
			img.onload = function() {
				var dataUrl = saveImageThumbnail(this, file, id, callback, uploadDoneCallback);
				
			};
		}
		
		function saveImageThumbnail(image, file, id, callback, uploadDoneCallback) {
			var dataUrl = createThumbnail(image, file.length);
			var blob;
			if (dataUrl == null) {
				//original is smaller in size, use that
				dataUrl = file;
			}
			//convert to binary
			blob = dataURItoBlob(dataUrl);
			//create flow thumbnail
			var FLOW_SIZE = 263;
			var dataUrlFlow = createThumbnail(image, file.length, FLOW_SIZE, true);
			
			pics[id.toString()].original = dataUrl;
			pics[id.toString()].flow = dataUrlFlow;
			callback();
			
			//ajax upload
			var formData = new FormData();
			formData.append("picture", blob);
			
			$http.post('/mystory/pictures', formData, {
				  headers: { 'Content-Type': undefined },
				  transformRequest: function(data) { return data; }
				})
			.success(function(data, status, headers, config) {
				remotePictures[data.id] = data;
				if (itemsWaitingForUuids[id]) {
					assignPicToItem(data.id, id, itemsWaitingForUuids[id]);
					delete itemsWaitingForUuids[id];
				} else {
					//TODO: handle case for cancelled upload					
					uuidWaitingForItem.value = data.id;
				}
				
				if (uploadDoneCallback) uploadDoneCallback();
				decUploads();
			}).error(function(data, status, headers, config) {
				//TODO: add error handler of some sort
			});
			
			//TODO: create flow thumbnail as well
			
		    /*$.ajax({
			  url: 'http://localhost:9000/mystory/pictures',
			  data: formData,
			  processData: false,
			  contentType: false,
			  type: 'POST',
			  success: function(data, textStatus){
			    alert(data);
			  },
			  error: function(jqXHR, textStatus, errorThrown) {
			  	alert(textStatus);
			  }
			});
			*/
		}

		function createThumbnail(image, origDataUrlLength, maxDim, forceSquare) {
			maxDim = (typeof maxDim === "undefined") ? 1500 : maxDim;
			forceSquare = (typeof forceSquare === "undefined") ? false : forceSquare;
			origDataUrlLength = (typeof origDataUrlLength === "undefined") ? 0 : origDataUrlLength;
			
			if (image.width < maxDim && image.height < maxDim)
				return null;
			
			var ratioWidth, ratioHeight;
			var thumbWidth, thumbHeight;
			
			if (image.width >= image.height) {
				ratioWidth = (forceSquare)? Math.floor(image.width * (maxDim/image.height)) : maxDim;
				ratioHeight = (forceSquare)? maxDim : Math.floor(maxDim * (image.height/image.width));
			} else {
				ratioWidth = (forceSquare)? maxDim : Math.floor(maxDim * (image.width/image.height));
				ratioHeight = (forceSquare)? Math.floor(image.height * (maxDim/image.width)) : maxDim;
				
			}
			if (forceSquare) {
				thumbWidth = maxDim;
				thumbHeight = maxDim;
			} else {
				thumbWidth = ratioWidth;
				thumbHeight = ratioHeight;
			}
				
			canvas.width = thumbWidth;
			canvas.height = thumbHeight;
			
			var sourceWidth = Math.floor(Math.min(ratioWidth / thumbWidth, thumbWidth / ratioWidth)  * image.width);
			var sourceHeight = Math.floor(Math.min(ratioHeight / thumbHeight, thumbHeight / ratioHeight) * image.height);
			var sourceX = (image.width - sourceWidth) / 2;
			var sourceY = (image.height - sourceHeight) / 2;
			
//			var drawX = Math.round((thumbWidth - ratioWidth) / 2.0);
//			var drawY = Math.round((thumbHeight - ratioHeight) / 2.0);
			
			context.fillStyle = 'white';
			context.fillRect ( 0, 0, thumbWidth, thumbHeight);
			context.drawImage(image, sourceX, sourceY, sourceWidth, sourceHeight, 0, 0, thumbWidth, thumbHeight);
			
			//context.drawImage(image, drawX, drawY, thumbWidth, thumbHeight);
			var compressionLevel = 1.0;
			var compressionInterval = 0.1;
			var dataUrl;
			do {
				compressionLevel -= compressionInterval;
				dataUrl = canvas.toDataURL('image/jpeg', compressionLevel);
				//compare to original size - if original is smaller, increase compression
			} while ((!origDataUrlLength) || (dataUrl.length > origDataUrlLength));
			
			return dataUrl;
		}
		
		function dataURItoBlob(dataURI) {
		    // convert base64 to raw binary data held in a string
		    // doesn't handle URLEncoded DataURIs
		    var byteString = atob(dataURI.split(',')[1]);

		    // separate out the mime component
		    var mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0];

		    // write the bytes of the string to an ArrayBuffer
		    var ab = new ArrayBuffer(byteString.length);
		    var ia = new Uint8Array(ab);
		    for (var i = 0; i < byteString.length; i++) {
		        ia[i] = byteString.charCodeAt(i);
		    }

		    // write the ArrayBuffer to a blob, and you're done
		    var blob = new Blob([ab], {type:mimeString});
		    return blob;
		}
		
		function assignPicToItem(picUuid, picLocalId, itemId) {
			var data = {"picture":picUuid};
			incUploads();//TODO: move increment to when item is added to waiting list?
			$http.post('/items/' + itemId, data)
			.success(function(data, status, headers, config) {
				decUploads();
			}).error(function(data, status, headers, config) {
				
			});
		}
		
		function assignWaitingPicToItem(itemId) {
			if (uuidWaitingForItem.value) {
				var data = {"picture":uuidWaitingForItem.value};
				uuidWaitingForItem.value = null;
				incUploads(); //TODO: move increment to when uuidWaitingForItem.value is assigned?  
				$http.post('/items/' + itemId, data)
				.success(function(data, status, headers, config) {
					decUploads();
				}).error(function(data, status, headers, config) {
					
				});
				return true;
			} else {
				return false;
			}
		}
}]);