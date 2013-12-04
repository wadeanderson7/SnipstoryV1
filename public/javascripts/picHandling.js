function hasFileReadSupport() {
	return (window.FileReader && window.FileList);
}

var imageDim, canvas, context;

$(document).ready(function() {

	if (!hasFileReadSupport()) {
		$("#error").html("Your browser is not supported.");
	}
	
	imageDim = 1500;
	canvas = document.createElement('canvas');
	context = canvas.getContext('2d');
	canvas.width = 1500;
	
	$("input[name='picFile']").change(function(evt) {
		var files = evt.target.files;
		if (files.length > 0) {
			var f = files[0]
			//handle first image only
			
			if (!f.type.match('image.*')) {
		        alert("Not an image file");
		    }
		    
		    var reader = new FileReader();
		    reader.onload = function(e) {
	    		loadImage(e.target.result);
	    	};
		}
		reader.readAsDataURL(f);
	
		//alert("TODO: load image");
	});

});

function loadImage(file) {
	var img = new Image();
	img.src = file;
	img.onload = function() {
		saveImageThumbnail(this, file.length);
	};
}

function saveImageThumbnail(image, origDataUrlLength) {
	var blob = createThumbnailBlob(image, origDataUrlLength);
	//ajax upload
	var formData = new FormData();
	formData.append("picture", blob);
	
    $.ajax({
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
	
}

function createThumbnailBlob(image, origDataUrlLength, maxDim, forceSquare) {
	maxDim = (typeof maxDim === "undefined") ? 1500 : maxDim;
	forceSquare = (typeof forceSquare === "undefined") ? false : forceSquare;
	origDataUrlLength = (typeof origDataUrlLength === "undefined") ? 0 : origDataUrlLength;
	
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
	//convert to binary
	var blob = dataURItoBlob(dataUrl);
	return blob;
}
/*
function drawThumbnail(image) {
	//find dimensions for thumbnail based on image size
	var thumbWidth, thumbHeight;
	if (image.width >= image.height) {
		thumbWidth = imageDim;
		thumbHeight = Math.round(imageDim * (image.height / image.width));
	} else {
		thumbHeight = imageDim;
		thumbWidth = Math.round(imageDim * (image.width / image.height));
	}
	canvas.width = thumbWidth;
	canvas.height = thumbHeight;
	
	context.fillStyle = 'white';
	context.fillRect ( 0, 0, thumbWidth, thumbHeight);
	context.drawImage(image, 0, 0, thumbWidth, thumbHeight);
	var dataUrl = canvas.toDataURL('image/jpeg', 0.9);
	alert(Math.round( dataUrl.length / 1000 * 100 ) / 100 + ' KB');
	//compare to original size - if original is smaller, use that instead
	//convert to binary
	var blob = dataURItoBlob(dataUrl);
	//ajax upload
	var formData = new FormData();
	formData.append("picture", blob);
	
    $.ajax({
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
};
*/

function dataURItoBlob(dataURI) {
    // convert base64 to raw binary data held in a string
    // doesn't handle URLEncoded DataURIs
    var byteString = atob(dataURI.split(',')[1]);

    // separate out the mime component
    var mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0]

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