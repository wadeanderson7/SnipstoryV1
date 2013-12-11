var snipStoryServices = angular.module('snipStoryServices', []);

snipStoryServices.factory('imageHandler', ['$http', '$document',
    function ($http, $document) {
		var canvas = document.createElement('canvas');
		var context = canvas.getContext('2d');
		canvas.width = 1500;
		
		var remotePictures = {};
		var pics = {};
		var nextId = 0;
		var numUploading = 0;
		
		function incUploads() {
			numUploading++;
		};
		function decUploads() {
			numUploading--;
		};
		function getNextId() {
			return nextId++;
		}
		
		return {
			pics : pics,
			doThumbnailAndUpload : function(file, callback) {
				if (!file.type.match('image.*')) {
			        return null;
			    }
				
				var reader = new FileReader();
				var id = getNextId();
				pics[id.toString()] = {original: null, flow: null};
			    reader.onload = function(e) {
		    		loadImage(e.target.result, id, callback);
		    	};
		    	reader.readAsDataURL(file);
		    	return id;
			}
		};
		
		function loadImage(file, id, callback) {
			incUploads();
			var img = new Image();
			img.src = file;
			img.onload = function() {
				var dataUrl = saveImageThumbnail(this, file, id, callback);
				
			};
		}
		
		function saveImageThumbnail(image, file, id, callback) {
			var dataUrl = createThumbnail(image, file.length);
			var blob;
			if (blob == null) {
				//original is smaller in size, use that
				//convert to binary
				blob = dataURItoBlob(file);
			} else {
				dataUrl = file;
				//convert to binary
				blob = dataURItoBlob(dataUrl);
			}
			
			pics[id.toString()].original = dataUrl;
			callback();
			
			//ajax upload
			var formData = new FormData();
			formData.append("picture", blob);
			
			//TODO: have some kind of status for when this is running
			/*
			$http.post('/mystory/pictures', formData, {
				  headers: { 'Content-Type': undefined },
				  transformRequest: function(data) { return data; }
				})
			.success(function(data, status, headers, config) {
				remotePictures[data.id] = data;
				alert("Image " + id + " saved as " + data.id);
				decUploads();
			}).error(function(data, status, headers, config) {
				//TODO: add error handler of some sort
			});
			*/
			
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
				ratioWidth = maxDim;
				ratioHeight = Math.round(maxDim * (image.height / image.width));
			} else {
				ratioHeight = maxDim;
				ratioWidth = Math.round(maxDim * (image.width / image.height));
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
			
			var drawX = Math.round((thumbWidth - ratioWidth) / 2.0);
			var drawY = Math.round((thumbHeight - ratioHeight) / 2.0);
			
			context.fillStyle = 'white';
			context.fillRect ( 0, 0, thumbWidth, thumbHeight);
			context.drawImage(image, drawX, drawY, thumbWidth, thumbHeight);
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
}]);