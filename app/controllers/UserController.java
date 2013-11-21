package controllers;

import models.snipstory.LifeStory;
import models.snipstory.StoryChapter;
import play.mvc.Controller;
import play.mvc.Result;

public class UserController extends Controller {

	public static Result create() {
		return ok();
	}
	
	public static Result test() {
		LifeStory story = new LifeStory();
		story.save();
		
		StoryChapter chapter = new StoryChapter();
		chapter.name = "Ch 1";
		chapter.startYear = 1995;
		chapter.endYear = 1996;			
		
		story.chapters.add(chapter);
		story.save();
		
		
		return ok();
	}
	
	public static Result test2(Long id) {
		LifeStory story = LifeStory.find.byId(id);
		story.delete();
		return ok();
	}
	
}
