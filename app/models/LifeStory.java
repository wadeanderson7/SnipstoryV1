package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import play.db.ebean.Model;

import com.avaje.ebean.annotation.PrivateOwned;

@Entity
public class LifeStory extends Model {
	
	public static Finder<Long, LifeStory> find = new Finder<Long, LifeStory>(Long.class, LifeStory.class);

	private static final long serialVersionUID = 1L;
	
	@Id
	public long id;
	
	@OneToOne
	public User user;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@PrivateOwned
	public List<StoryChapter> chapters;
}
