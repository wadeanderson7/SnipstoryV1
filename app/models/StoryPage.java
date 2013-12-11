package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.ebean.Model;
import play.libs.Json;

import com.avaje.ebean.annotation.PrivateOwned;
import com.fasterxml.jackson.databind.JsonNode;

@Entity
public class StoryPage extends Model implements JsonMappable {

	private static final long serialVersionUID = 1L;
	
	public static Finder<Long, StoryPage> find = new Finder<Long, StoryPage>(Long.class, StoryPage.class);

	@Id
	public long id;
	
	public String name;
	public String description;
	
	@Column(nullable = false)
	public long ordering;
	
	@ManyToOne
	public StoryChapter storyChapter;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@PrivateOwned
	public List<StoryItem> items;
	
	@Override
	public JsonNode toJson() {
		return toJson(false);
	}
	
	@Override
	public JsonNode toJson(boolean showChildren) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("name", name);
		map.put("description", description);
		map.put("ordering", ordering);
		map.put("chapter", storyChapter.id);
		if (showChildren) {
			ArrayList<JsonNode> itemList = new ArrayList<JsonNode>();
			for (StoryItem i : items)
				itemList.add(i.toJson(true));
			map.put("items", itemList);
		} else {
			ArrayList<Long> itemList = new ArrayList<Long>();
			for (StoryItem i : items)
				itemList.add(i.id);
			map.put("items", itemList);
		}
		return Json.toJson(map);
	}

	@Override
	public boolean applyJson(JsonNode node) {
		if (node.has("name")) {
			String newName = JsonHelper.getNonEmptyString(node,"name");
			if (newName == null)
				return false;
			name = newName;
		}
		if (node.has("description")) {
			String newDesc = JsonHelper.getNonEmptyString(node,"description");
			description = newDesc;
		}
		if (node.has("ordering")) {
			Long newOrdering = JsonHelper.getLong(node,"ordering");
			if (newOrdering == null)
				return false;
			ordering = newOrdering;
		}
		//TODO?: make chapterId editable?
		return true;
	}

	public static boolean owns(Long userId, StoryPage page) {
		if (page == null)
			return false;
		if (userId != page.storyChapter.lifeStory.user.id)
			return false;
		return true;
	}
		
}
