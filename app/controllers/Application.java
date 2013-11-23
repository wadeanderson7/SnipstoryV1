package controllers;

import static play.data.Form.form;
import models.snipstory.User;
import play.*;
import play.data.Form;
import play.data.validation.Constraints.Required;
import play.mvc.*;
import views.html.*;

public class Application extends Controller {

	@Security.Authenticated(SignedIn.class)
    public static Result index() {
        return ok(index.render("You are signed in."));
    }
    
    public static Result login() {
    	return ok(login.render(form(Login.class), form(User.class)));
    }
    
    public static Result logout() {
    	session().clear();
        flash("logout", "You've have signed out");
        return redirect(
            routes.Application.login()
        );
    }
    
    public static Result authenticate() {
    	Form<Login> loginForm = form(Login.class).bindFromRequest();
        if (loginForm.hasErrors()) {
            return badRequest(login.render(loginForm, form(User.class)));
        } else {
            session().clear();
            User user = User.find.where().eq("email", loginForm.get().email).findUnique();
            session("uid", Long.toString(user.id));
            return redirect(
                routes.Application.index()
            );
        }
    }
    
    public static Result register() {
    	return TODO;
    }
    
    public static Result accountRecover() {
    	return TODO;
    }
    
    public static Result userInfo() {
    	return TODO;
    }
    
    public static class Login {

    	@Required
        public String email;
    	@Required
        public String passwordHash;

        public String validate() {
            if (User.authenticate(email, passwordHash) == null) {
              return "Invalid email or password";
            }
            return null;
        }
    }
}
