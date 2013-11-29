package controllers;

import models.StoryChapter;
import models.StoryItem;
import models.StoryPage;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security.Authenticator;

public class UserSignedIn extends Authenticator {
	@Override
    public String getUsername(Context ctx) {
        return ctx.session().get("uid");
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(routes.Application.login());
    }

	public static boolean hasChapter(StoryChapter chapter) {
		return StoryChapter.owns(Users.getSessionUid(), chapter);
	}
	
	public static boolean hasPage(StoryPage page) {
		return StoryPage.owns(Users.getSessionUid(), page);
	}
	
	public static boolean hasItem(StoryItem item) {
		return StoryItem.owns(Users.getSessionUid(), item);
	}
}
