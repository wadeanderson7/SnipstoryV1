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
	
	@Column(nullable = false)
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
	public boolean applyJson(JsonNode node) {
		if (node.has("name")) {
			String newName = JsonHelper.getNonEmptyString(node,"name");
			if (newName == null)
				return false;
			name = newName;
		}
		if (node.has("ordering")) {
			Long newOrdering = JsonHelper.getLong(node,"ordering");
			if (newOrdering == null)
				return false;
			ordering = newOrdering;
		}
		if (node.has("startYear")) {
			Integer newYear = JsonHelper.getInteger(node,"startYear");
			if (newYear != null && newYear < 0)
				return false;
			startYear = newYear;
		}
		if (node.has("endYear")) {
			Integer newYear = JsonHelper.getInteger(node,"endYear");
			if (newYear != null && newYear < 0)
				return false;
			endYear = newYear;
		}
		return true;
	}
	
	public static boolean owns(long userId, StoryChapter chapter) {
		if (chapter == null)
			return false;
		if (userId != chapter.lifeStory.user.id)
			return false;
		return true;
	}
}
