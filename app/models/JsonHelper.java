package models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

public final class JsonHelper {

	public static String getString(JsonNode node, String property) {
		JsonNode value = node.get(property); 
		if (value.isNull())
			return null;
		else {
			return value.asText();
		}
	}
	
	public static String getNonEmptyString(JsonNode node, String property) {
		//string must be non-null and non-empty (including white-space)
		String result = getString(node, property);
		if (result == null || result.trim().isEmpty()) {
			return null;
		} else {
			return result;
		}
	}

	public static Long getLong(JsonNode node, String property) {
		JsonNode value = node.get(property);
		if (value.isNull() || !value.canConvertToLong())
			return null;
		else {
			return value.asLong();
		}
	}

	public static Integer getInteger(JsonNode node, String property) {
		JsonNode value = node.get(property);
		if (value.isNull())
			return null;
		else {
			return value.asInt();
		}
	}

	public static Object getValueOrNull(Object obj) {
		if (obj == null)
			return NullNode.getInstance();
		else
			return obj;
	}

	public static <T extends Enum<T>> T getEnum(Class<T> enumType, JsonNode node, String property) {
		JsonNode value = node.get(property);
		try {
			return T.valueOf(enumType, value.asText());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	public static List<JsonNode> listToJsonList(List<? extends JsonMappable> list) {
		List<JsonNode> result = new ArrayList<JsonNode>(list.size()); 
		for(JsonMappable item : list) {
			result.add(item.toJson());
		}
		return result;
	}

}
