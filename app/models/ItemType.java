package models;

import com.avaje.ebean.annotation.EnumValue;

public enum ItemType {

	@EnumValue("A")
	AUDIO,
	
	@EnumValue("I")
	IMAGE,
	
	@EnumValue("T")
	TEXT,
	
	@EnumValue("V")
	VIDEO
}
