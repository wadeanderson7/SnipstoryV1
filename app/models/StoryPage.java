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
	
	@Column(nullable = false)
	public long ordering;
	
	@ManyToOne
	public StoryChapter storyChapter;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@PrivateOwned
	public List<StoryItem> items;
	
	@Override
	public JsonNode toJson() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("ordering", ordering);
		map.put("chapter", storyChapter.id);
		ArrayList<Long> itemList = new ArrayList<Long>();
		for (StoryItem i : items)
			itemList.add(i.id);
		map.put("items", itemList);
		return Json.toJson(map);
	}

	@Override
	public void applyJson(JsonNode node) {
		if (node.has("name"))
			name = JsonHelper.getString(node,"name");
		if (node.has("ordering"))
			ordering = JsonHelper.getLong(node,"ordering");
		//TODO?: make chapterId editable?
	}
		
}
