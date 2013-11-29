package controllers;

import models.LifeStory;
import models.StoryChapter;
import models.StoryItem;
import models.StoryPage;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

@Security.Authenticated(SignedIn.class)
public class LifeStories extends Controller {

	public static Result getMyStory() {
		LifeStory story = LifeStory.find.where().eq("user_id", request().username()).findUnique();
		if (story == null) {
			//create story for the user
			User user = Users.getSessionUserRef();
			story = new LifeStory();
			story.user = user;
			story.save();
		}
		return ok(story.toJson());
	}
	
	public static Result getChapter(Long id) {
		StoryChapter chapter = StoryChapter.find.byId(id);
		if (chapter == null)
			return notFound();
		
		//verify this chapter belongs to the user
		if (Users.getSessionUid() != chapter.lifeStory.user.id)
			return notFound();
		
		return ok(chapter.toJson());
	}
	
	public static Result getPage(Long id) {
		StoryPage page = StoryPage.find.byId(id);
		if (page == null)
			return notFound();
		
		//verify this page belongs to the user
		if (Users.getSessionUid() != page.storyChapter.lifeStory.user.id)
			return notFound();
		
		return ok(page.toJson());
	}
	
	public static Result getItem(Long id) {
		StoryItem item = StoryItem.find.byId(id);
		if (item == null)
			return notFound();
		
		//verify this item belongs to the user
		if (Users.getSessionUid() != item.storyPage.storyChapter.lifeStory.user.id)
			return notFound();
		
		return ok(item.toJson());
	}
}
