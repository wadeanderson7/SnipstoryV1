package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;

@Entity
public class StoryItem extends Model {

	private static final long serialVersionUID = 1L;
	
	@Id
	public long id;
	
	public String url;
	public ItemType type;
	
	@Lob
	public String description;
	
	public long ordering;
	
	@ManyToOne
	public StoryPage storyPage;
	
}
