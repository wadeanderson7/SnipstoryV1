package controllers;

import static play.data.Form.form;
import models.snipstory.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import controllers.Application.Login;
import controllers.Application.RecoverAccount;
//import com.typesafe.plugin.*;

public class Users extends Controller {
	
	static Form<User> newUserForm = Form.form(User.class);

	public static Result create() {
		Form<User> filledForm = newUserForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.login.render(form(Login.class), filledForm));
		} else {
			User newUser = filledForm.get();
			newUser.prepForCreate();
			newUser.save();
			session().clear();
            session("uid", Long.toString(newUser.id));
            return redirect(
                routes.Application.index()
            );
		}
	}
	
	public static Result recover() {
    	Form<RecoverAccount> form = form(RecoverAccount.class).bindFromRequest();
    	if (form.hasErrors()) {
    		return badRequest(views.html.accountRecover.render(form));
    	} else {
    		//TODO: send email
    		String email = form.get().email;
    		User user = User.find.where().eq("email", email).findUnique();
//    		MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
//    		mail.setSubject("Recover your SnipStory account");
//    		mail.setRecipient(user.name + " <" + email + ">");
//    		mail.setFrom("SnipStory <noreply@alpha.snipstory.com");
//    		mail.send("TODO: send an actual recovery link");
    		//TODO?: does this need to be done in a separate thread in some way?
    		flash("login", "Recovery email sent to " + email);
    		
    		return redirect(routes.Application.login());
    	}
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
