package models;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonMappable {
	public JsonNode toJson();
	public void applyJson(JsonNode node);
}
