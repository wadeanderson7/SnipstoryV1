package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import play.db.ebean.Model;
import play.libs.Json;

import com.avaje.ebean.annotation.PrivateOwned;
import com.fasterxml.jackson.databind.JsonNode;

@Entity
public class LifeStory extends Model implements JsonMappable {
	
	public static Finder<Long, LifeStory> find = new Finder<Long, LifeStory>(Long.class, LifeStory.class);

	private static final long serialVersionUID = 1L;
	
	public static final long ORDERING_INTERVAL = 1000000;
	
	@Id
	public long id;
	
	@OneToOne
	public User user;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@PrivateOwned
	public List<StoryChapter> chapters;

	@Override
	public JsonNode toJson() {
		return toJson(false);
	}

	@Override
	public JsonNode toJson(boolean showChildren) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("user", user.id);
		if (showChildren) {
			ArrayList<JsonNode> chapterList = new ArrayList<JsonNode>();
			for (StoryChapter c : chapters)
				chapterList.add(c.toJson(true));
			map.put("chapters", chapterList);
		} else {
			ArrayList<Long> chapterList = new ArrayList<Long>();
			for (StoryChapter c : chapters)
				chapterList.add(c.id);
			map.put("chapters", chapterList);
		}
		return Json.toJson(map);
	}

	@Override
	public boolean applyJson(JsonNode node) {
		//no properties are writable from json
		return true;
	}	
}
