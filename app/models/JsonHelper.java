package models;

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

	public static long getLong(JsonNode node, String property) {
		JsonNode value = node.get(property); 
		return value.asLong();
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
		return T.valueOf(enumType, value.asText());
	}

}
