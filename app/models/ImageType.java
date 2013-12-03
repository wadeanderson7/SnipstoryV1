package models;

import com.avaje.ebean.annotation.EnumValue;

public enum ImageType {

	@EnumValue("jpeg")
	JPEG,
	@EnumValue("png")
	PNG,
	@EnumValue("gif")
	GIF;
	
	public static ImageType getFromContentType(String contentType) {
		switch(contentType) {
		case "image/jpeg":
			return JPEG;
		case "image/png":
			return PNG;
		case "image/gif":
			return GIF;
		default:
			return null;
		}
	}
	
	public static ImageType getFromString(String desc) {
		ImageType result = getFromContentType(desc);
		if (result != null)
			return result;
		else {
			return valueOf(desc.toUpperCase());
		}
	}
	
	public String getExtension() {
		return toString().toLowerCase();
	}
}
