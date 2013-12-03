package models;

public enum ThumbnailSize {
	SMALL(150), FLOW(300), MEDIUM(500), LARGE(1000), ORIGINAL(-1);
	
	private int maxDim;
	
	private ThumbnailSize(int maxDimension) {
		this.maxDim = maxDimension;
	}
	
	public String getUrlSuffix(String extension) {
		if (maxDim > 0) {
			return "thumb_" + this.toString() + "." + extension;
		} else {
			return extension;
		}
	}

	public int getWidth(int width, int height) {
		//calculate max width that will fit
		if (maxDim <= 0) {
			return width;
		}
		
		if (width >= height) {
			return width;
		} else {
			double ratio = width / (double)height;
			return (int)(maxDim * ratio);
		}
	}
	
	public int getHeight(int width, int height) {
		//calculate max height that will fit
		if (maxDim == 0) {
			return height;
		}
		
		if (height >= width) {
			return height;
		} else {
			double ratio = height / (double)width;
			return (int)(maxDim * ratio);
		}
	}
	
	public int getMaxDim() {
		return maxDim;
	}
}
