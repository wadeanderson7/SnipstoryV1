package models;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonMappable {
	public JsonNode toJson();
	public JsonNode toJson(boolean showChildren);
	public boolean applyJson(JsonNode node);
}
