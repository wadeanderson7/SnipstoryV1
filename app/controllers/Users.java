package controllers;

import static play.data.Form.form;
import controllers.Application.Login;
import models.snipstory.LifeStory;
import models.snipstory.StoryChapter;
import models.snipstory.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

public class Users extends Controller {
	
	static Form<User> newUserForm = Form.form(User.class);

	public static Result create() {
		Form<User> filledForm = newUserForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.login.render(form(Login.class), filledForm));
		} else {
			User formUser = filledForm.get();
			User newUser = new User(formUser.email, formUser.name, formUser.birthdate);
			newUser.save();
			session().clear();
            session("uid", Long.toString(newUser.id));
            return redirect(
                routes.Application.index()
            );
		}
	}
	
	public static Result recover() {
		return TODO;
	}
	
	public static Result setPasswordHash(Long id) {
		return TODO;
	}
	
	public static Result setInfo(Long id) {
		return TODO;
	}
	
	public static Result getInfo(Long id) {
		return TODO;
	}
}
