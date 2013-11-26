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
		User user = User.find.byId(Long.parseLong(session().get("uid")));
        return ok(index.render(user, "You are signed in."));
    }
    
    public static Result login() {
    	if (session().get("uid") != null)
    		return redirect(routes.Application.index());
    	else
    		return ok(login.render(form(Login.class), form(User.class)));
    }
    
    public static Result logout() {
    	session().clear();
        flash("login", "You have signed out");
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
        
    public static Result accountRecover() {
    	return ok(views.html.accountRecover.render(form(RecoverAccount.class)));
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
    
    public static class RecoverAccount {

    	@Required
        public String email;

        public String validate() {
        	if (User.find.where().eq("email", email).findRowCount() == 1) {
        		return null;
        	} else { 
        		//TODO?: should we validate this? 
        		//       (malicious user could look up if a particular email address exists in the system) 
        		return "No account with that email exists";
        	}
        }
    }
}
