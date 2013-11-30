package models;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;
import play.libs.Json;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.UserSignedIn;

@Entity
public class StoryItem extends Model implements JsonMappable {

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
	public boolean applyJson(JsonNode node) {
		if (node.has("description")) {
			String newDescription = JsonHelper.getString(node,"description");
			if (newDescription.trim().isEmpty())
				return false;
			description = newDescription;
		}
		if (node.has("url")) {
			String newUrl = JsonHelper.getString(node,"url");
			try {
				URL test = new URL(newUrl);
				if (test.getProtocol() != "https")
					return false;
				if (!test.getAuthority().equals(play.Play.application().configuration().getString("snipstory.picDomain")))
					return false;
				//TODO: additional validation on URL for S3 pictures
			} catch (MalformedURLException e) {
				return false;
			}
			url = newUrl;
		}
		if (node.has("type")) {
			ItemType newType = JsonHelper.getEnum(ItemType.class, node, "type");
			if (type == null)
				return false;
			type = newType;
		}
		if (node.has("ordering")) {
			Long newOrdering = JsonHelper.getLong(node,"ordering");
			if (newOrdering == null) {
				return false;
			}
			ordering = newOrdering;
		}
		if (node.has("page")) {
			long newPage = JsonHelper.getLong(node,"page");
			if (newPage != storyPage.id) {
				if (!UserSignedIn.hasPage(StoryPage.find.ref(newPage)))
					return false;
				storyPage = StoryPage.find.ref(newPage);
			}
		}
		return true;
	}

	public static boolean owns(Long userId, StoryItem item) {
		if (item == null)
			return false;
		if (userId != item.storyPage.storyChapter.lifeStory.user.id)
			return false;
		return true;
	}
}
