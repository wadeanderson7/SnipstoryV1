package controllers;

import com.fasterxml.jackson.databind.JsonNode;

import models.JsonMappableModel;
import models.LifeStory;
import models.StoryChapter;
import models.StoryItem;
import models.StoryPage;
import models.User;
import play.mvc.*;

@Security.Authenticated(UserSignedIn.class)
public class LifeStories extends Controller {

	public static Result getMyStory() {
		LifeStory story = getUserStory();
		return ok(story.toJson());
	}
	
	private static LifeStory getUserStory() {
		LifeStory story = LifeStory.find.where().eq("user_id", request().username()).findUnique();
		if (story == null) {
			//create story for the user
			User user = Users.getSessionUserRef();
			story = new LifeStory();
			story.user = user;
			story.save();
		}
		return story;
	}
	
	public static Result getChapter(Long id) {
		StoryChapter chapter = StoryChapter.find.byId(id);
		if (!UserSignedIn.hasChapter(chapter))
			return notFound();
		
		return ok(chapter.toJson());
	}
	
	public static Result getPage(Long id) {
		StoryPage page = StoryPage.find.byId(id);
		if (!UserSignedIn.hasPage(page))
			return notFound();
		
		return ok(page.toJson());
	}
	
	public static Result getItem(Long id) {
		StoryItem item = StoryItem.find.byId(id);
		if (!UserSignedIn.hasItem(item))
			return notFound();
		
		return ok(item.toJson());
	}
	
	@BodyParser.Of(BodyParser.Json.class)
	public static Result createChapter() {
		LifeStory story = getUserStory();
		StoryChapter chapter = new StoryChapter();
		chapter.lifeStory = story;
		
		JsonNode node = request().body().asJson();
		chapter.applyJson(node);
		chapter.save();
		return ok(chapter.toJson());
	}
	
	@BodyParser.Of(BodyParser.Json.class)
	public static Result createPage(Long chapterId) {
		StoryChapter chapter = StoryChapter.find.byId(chapterId);
		if (!UserSignedIn.hasChapter(chapter))
			return notFound();
		
		StoryPage page = new StoryPage();
		page.storyChapter = chapter;
		
		JsonNode node = request().body().asJson();
		page.applyJson(node);
		page.save();
		return ok(page.toJson());
	}
	
	@BodyParser.Of(BodyParser.Json.class)
	public static Result createItem(Long pageId) {
		StoryPage page = StoryPage.find.byId(pageId);
		if (!UserSignedIn.hasPage(page))
			return notFound();
		
		StoryItem item = new StoryItem();
		item.storyPage = page;
		
		JsonNode node = request().body().asJson();
		item.applyJson(node);
		item.save();
		return ok(item.toJson());
	}
	
	
	public static Result editChapter(Long chapterId) {
		StoryChapter chapter = StoryChapter.find.byId(chapterId);
		if (!UserSignedIn.hasChapter(chapter))
			return notFound();
		editModel(chapter);
		return ok(chapter.toJson());
	}
	
	public static Result editPage(Long pageId) {
		StoryPage page = StoryPage.find.byId(pageId);
		if (!UserSignedIn.hasPage(page))
			return notFound();
		editModel(page);
		return ok(page.toJson());
	}
	
	public static Result editItem(Long itemId) {
		StoryItem item = StoryItem.find.byId(itemId);
		if (!UserSignedIn.hasItem(item))
			return notFound();
		editModel(item);
		return ok(item.toJson());
	}
	
	private static void editModel(JsonMappableModel model) {
		JsonNode node = request().body().asJson();
		model.applyJson(node);
		model.save();
	}
	
	
	public static Result deleteChapter(Long chapterId) {
		StoryChapter chapter = StoryChapter.find.byId(chapterId);
		if (!UserSignedIn.hasChapter(chapter))
			return notFound();
		chapter.delete();
		return ok();
	}
	
	public static Result deletePage(Long pageId) {
		StoryPage page = StoryPage.find.byId(pageId);
		if (!UserSignedIn.hasPage(page))
			return notFound();
		page.delete();
		return ok(page.toJson());
	}
	
	public static Result deleteItem(Long itemId) {
		StoryItem item = StoryItem.find.byId(itemId);
		if (!UserSignedIn.hasItem(item))
			return notFound();
		item.delete();
		return ok();
	}
}
