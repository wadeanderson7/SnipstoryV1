package models;

public enum ThumbnailSize {
	SMALL(150), FLOW(263, true), MEDIUM(500), LARGE(1000), ORIGINAL(-1);
	
	private int maxDim;
	private boolean forceSquare;
	
	private ThumbnailSize(int maxDimension) {
		this(maxDimension, false);
	}
	
	private ThumbnailSize(int maxDimension, boolean forceSquare) {
		maxDim = maxDimension;
		this.forceSquare = forceSquare;
	}
	
	public String getUrlSuffix(String extension) {
		if (maxDim > 0) {
			return "thumb_" + this.toString().toLowerCase() + "." + extension;
		} else {
			return extension;
		}
	}
	
	public int getWidth(int width, int height) {
		if (maxDim <= 0) {
			return width;
		}
		if (forceSquare) {
			return maxDim;
		} else {
			//get dimensions to fit picture inside square
			if (width >= height) {
				return maxDim;
			} else {
				double ratio = width / (double)height;
				return (int)(maxDim * ratio);
			}
		}
	}
	
	public int getRatioWidth(int width, int height) {
		if (maxDim <= 0) {
			return width;
		}
		if (forceSquare) {
			//get dimensions outside picture size
			if (width >= height) {
				double ratio = maxDim / (double)height;
				return (int)(width * ratio);
			} else {
				return maxDim;
			}
		} else {
			//get dimensions to fit picture inside square
			if (width >= height) {
				return maxDim;
			} else {
				double ratio = width / (double)height;
				return (int)(maxDim * ratio);
			}
		}
	}

	public int getHeight(int width, int height) {
		if (maxDim <= 0) {
			return height;
		}
		if (forceSquare) {
			return maxDim;
		} else {
			//get dimensions to fit picture inside square
			if (height >= width) {
				return maxDim;
			} else {
				double ratio = height / (double)width;
				return (int)(maxDim * ratio);
			}
		}
	}
	
	public int getRatioHeight(int width, int height) {
		if (maxDim <= 0) {
			return height;
		}
		if (forceSquare) {
			//get dimensions outside picture size
			if (height >= width) {
				double ratio = maxDim / (double)width;
				return (int)(height * ratio);
			} else {
				return maxDim;
			}
		} else {
			//get dimensions to fit picture inside square
			if (height >= width) {
				return maxDim;
			} else {
				double ratio = height / (double)width;
				return (int)(maxDim * ratio);
			}
		}
	}
	
	public int getMaxDim() {
		return maxDim;
	}

	public String getContentType(ImageType type) {
		if (this.maxDim <= 0) {
			return type.getContentType();
		} else { 
			//TODO?: support thumbnails in formats other than JPEG
			return ImageType.JPEG.getContentType();
		}
	}
}
