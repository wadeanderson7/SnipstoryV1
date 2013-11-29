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
public class StoryChapter extends Model implements JsonMappable {

	private static final long serialVersionUID = 1L;
	
	public static Finder<Long, StoryChapter> find = new Finder<Long, StoryChapter>(Long.class, StoryChapter.class);

	@Id
	public long id;
	
	public String name;
	
	
	public Integer startYear;
	public Integer endYear;
	
	@Column(nullable = false)
	public long ordering;
	
	@ManyToOne
	public LifeStory lifeStory;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@PrivateOwned
	List<StoryPage> pages;

	@Override
	public JsonNode toJson() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("startYear", JsonHelper.getValueOrNull(startYear));
		map.put("endYear", JsonHelper.getValueOrNull(endYear));
		map.put("ordering", ordering);
		map.put("story", lifeStory.id);
		ArrayList<Long> pageList = new ArrayList<Long>();
		for (StoryPage p : pages)
			pageList.add(p.id);
		map.put("pages", pageList);
		return Json.toJson(map);
	}

	@Override
	public void applyJson(JsonNode node) {
		if (node.has("name"))
			name = JsonHelper.getString(node,"name");
		if (node.has("ordering"))
			ordering = JsonHelper.getLong(node,"ordering");
		if (node.has("startYear"))
			startYear = JsonHelper.getInteger(node,"startYear");
		if (node.has("endYear"))
			startYear = JsonHelper.getInteger(node,"endYear");
	}
}
