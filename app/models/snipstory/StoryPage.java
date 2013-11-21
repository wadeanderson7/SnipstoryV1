package models.snipstory;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.ebean.Model;

import com.avaje.ebean.annotation.PrivateOwned;

@Entity
public class StoryPage extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public long id;
	
	public String name;
	
	public long ordering;
	
	@ManyToOne
	public StoryChapter storyChapter;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@PrivateOwned
	public List<StoryItem> pages;
		
}
