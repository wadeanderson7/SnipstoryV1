package models;

import java.util.HashMap;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import play.db.ebean.Model;
import play.libs.Json;

import com.avaje.ebean.annotation.PrivateOwned;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import controllers.UserSignedIn;

@Entity
public class StoryItem extends Model implements JsonMappable {

	private static final long serialVersionUID = 1L;
	
	public static Finder<Long, StoryItem> find = new Finder<Long, StoryItem>(Long.class, StoryItem.class);
	
	@Id
	public long id;
	
	@OneToOne(cascade = CascadeType.ALL)
	@PrivateOwned
	public Picture picture;
	
	public ItemType type;
	
	@Lob
	public String description;
	
	public long ordering;
	
	@ManyToOne
	public StoryPage storyPage;
	
	@Override
	public JsonNode toJson() {
		return toJson(false);
	}
	
	@Override
	public JsonNode toJson(boolean showChildren) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("description", description);
		map.put("picture", (picture != null)? picture.toJson() : NullNode.getInstance());
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
		if (node.has("picture")) {
			JsonNode pictureNode = node.get("picture");
			if (pictureNode.isNull()) {
				picture = null;
			} else {
				if (!node.has("uuid"))
					return false;
				try {
					UUID uuid = UUID.fromString(node.get("uuid").asText());
					Picture pic = Picture.find.byId(uuid);
					if (!UserSignedIn.hasPicture(pic))
						return false;
					picture = pic;
				} catch (IllegalArgumentException e) {
					return false;
				}
			}
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
