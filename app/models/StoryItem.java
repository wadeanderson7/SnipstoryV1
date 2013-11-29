package models;

import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import play.libs.Json;

import com.fasterxml.jackson.databind.JsonNode;

@Entity
public class StoryItem extends JsonMappableModel {

	private static final long serialVersionUID = 1L;
	
	public static Finder<Long, StoryItem> find = new Finder<Long, StoryItem>(Long.class, StoryItem.class);
	
	@Id
	public long id;
	
	public String url;
	public ItemType type;
	
	@Lob
	public String description;
	
	public long ordering;
	
	@ManyToOne
	public StoryPage storyPage;
	
	@Override
	public JsonNode toJson() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("description", description);
		map.put("url", url);
		map.put("type", type);
		map.put("ordering", ordering);
		map.put("page", storyPage.id);
		return Json.toJson(map);
	}

	@Override
	public void applyJson(JsonNode node) {
		if (node.has("description"))
			description = JsonHelper.getString(node,"description");
		if (node.has("url"))
			url = JsonHelper.getString(node,"url");
		if (node.has("type"))
			type = JsonHelper.getEnum(ItemType.class, node, "type");
		if (node.has("ordering"))
			ordering = JsonHelper.getLong(node,"ordering");
		if (node.has("page")) {
			//TODO?: Where do we do a permissions check on the pageId (ensure new pages exists and belongs to user?)
			long newPage = JsonHelper.getLong(node,"page");
			if (newPage != storyPage.id) {
				storyPage = StoryPage.find.ref(newPage);
			}
		}
	}

	public static boolean owns(Long userId, StoryItem item) {
		if (item == null)
			return false;
		if (userId != item.storyPage.storyChapter.lifeStory.user.id)
			return false;
		return true;
	}
}
