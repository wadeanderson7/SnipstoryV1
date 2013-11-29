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

import play.libs.Json;

import com.avaje.ebean.annotation.PrivateOwned;
import com.fasterxml.jackson.databind.JsonNode;

@Entity
public class LifeStory extends JsonMappableModel {
	
	public static Finder<Long, LifeStory> find = new Finder<Long, LifeStory>(Long.class, LifeStory.class);

	private static final long serialVersionUID = 1L;
	
	@Id
	public long id;
	
	@OneToOne
	public User user;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@PrivateOwned
	public List<StoryChapter> chapters;

	@Override
	public JsonNode toJson() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("user", user.id);
		ArrayList<Long> chapterList = new ArrayList<Long>();
		for (StoryChapter c : chapters)
			chapterList.add(c.id);
		map.put("chapters", chapterList);
		return Json.toJson(map);
	}

	@Override
	public void applyJson(JsonNode node) {
		//no properties are writable from json
	}
}
