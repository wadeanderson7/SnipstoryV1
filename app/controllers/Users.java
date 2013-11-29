package controllers;

import static play.data.Form.form;
import models.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;

import controllers.Application.Login;
import controllers.Application.RecoverAccount;
import controllers.Application.ResetPassword;

public class Users extends Controller {
	
	static Form<User> newUserForm = Form.form(User.class);
	
	public static Result create() {
		Form<User> filledForm = newUserForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.login.render(form(Login.class), filledForm));
		} else {
			User newUser = filledForm.get();
			newUser.prepForCreate();
			sendVerifyEmail(newUser);
			newUser.save();
			Users.setUpSession(newUser);
            return redirect(
                routes.Application.index()
            );
		}
	}
	
	public static Result resetPassword(String resetToken) {
		Form<ResetPassword> form = form(ResetPassword.class).bindFromRequest();
		User user = User.find.where().eq("reset_token", resetToken).findUnique();
		if (form.hasErrors()) {
			return badRequest(views.html.resetPassword.render(user, form));
		} else if (user == null) {
			flash("message", "Reset link has expired, please try again");
			return redirect(routes.Application.accountRecover());
		} else {
			//change password and log in
			user.setNewPasswordViaReset(form.get().passwordHash);
			Users.setUpSession(user);
			user.save();
			return redirect(routes.Application.index());
		}
	}
	
	public static void setUpSession(User user) {
		setUpSession(user, false);
	}
	
	public static void setUpSession(User user, boolean createUser) {
		if (session().get("uid") == null && !createUser) {
			user.numLogins++;
		}
		session().clear();
		session("uid", Long.toString(user.id));
		session("start", Long.toString(System.currentTimeMillis()));
	}

	public static Result recover() {
    	Form<RecoverAccount> form = form(RecoverAccount.class).bindFromRequest();
    	if (form.hasErrors()) {
    		return badRequest(views.html.accountRecover.render(form));
    	} else {
    		String email = form.get().email;
    		User user = User.find.where().eq("email", email).findUnique();
    		
    		//generate reset token
    		user.createResetToken();
    		user.save();
    		String url = routes.Users.resetPassword(user.resetToken).absoluteURL(request());
    		
    		//TODO: send email
    		//TODO?: does this need to be done in a separate thread in some way?
    		MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
    		mail.setSubject("Recover your SnipStory account");
    		mail.setReplyTo("SnipStory <noreply@alpha.snipstory.com>");
    		mail.addFrom("SnipStory <noreply@alpha.snipstory.com>");
    		mail.addRecipient(user.name + " <" + email + ">");
    		mail.send("Click the link to reset password:\r\n\r\n" + url);
    		
    		flash("login", "Recovery email sent to " + email);
    		
    		return redirect(routes.Application.login());
    	}
	}
	
	public static void sendVerifyEmail(User user) {
		user.createResetToken(); //TODO?: use separate token from reset token?
		String url = routes.Users.verifyEmail(user.resetToken).absoluteURL(request());
		
		//TODO: send email
		//TODO?: does this need to be done in a separate thread in some way?
		MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
		mail.setSubject("Verify your SnipStory account");
		mail.setReplyTo("SnipStory <noreply@alpha.snipstory.com>");
		mail.addFrom("SnipStory <noreply@alpha.snipstory.com>");
		mail.addRecipient(user.name + " <" + user.email + ">");
		mail.send("Click the link to verify your email address with SnipStory:\r\n\r\n" + url);
	}
	
	public static Result verifyEmail(String token) {
		User user = User.find.where().eq("reset_token", token).findUnique();
		if (user == null) {
			flash("login","Your email address could not be verified.");
			return redirect(routes.Application.login());
		} else {
			user.verifyEmail();
			Users.setUpSession(user);
			user.save();
			flash("message","Your email address has been verified.");
			return redirect(routes.Application.index());
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
	
	public static User getSessionUser() {
		User user = User.find.byId(Long.parseLong(session().get("uid")));
		return user;
	}
	
	public static User getSessionUserRef() {
		User user = User.find.ref(Long.parseLong(session().get("uid")));
		return user;
	}
	
	public static Long getSessionUid() {
		String uid = session().get("uid");
		if (uid != null) {
			return Long.parseLong(uid);
		} else {
			return null;
		}
	}

}
