package controllers;

import static play.data.Form.form;
import models.User;
import play.*;
import play.data.Form;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.mvc.*;
import views.html.*;

public class Application extends Controller {

	@Security.Authenticated(UserSignedIn.class)
    public static Result index() {
		User user = Users.getSessionUser();
        return ok(index.render(user, ""));
    }
	
	public static Result landing() {
		if (Users.getSessionUid() != null)
    		return redirect(routes.Application.index());
    	else
    		return ok(landing.render());
	}
	
	public static Result about() {
		if (Users.getSessionUid() != null)
    		return redirect(routes.Application.index());
    	else
    		return ok(about.render());
	}
    
    public static Result login() {
    	if (Users.getSessionUid() != null)
    		return redirect(routes.Application.index());
    	else
    		return ok(login.render(form(Login.class)));
    }
    
    public static Result register() {
    	if (Users.getSessionUid() != null)
    		return redirect(routes.Application.index());
    	else
    		return ok(register.render(form(User.class)));
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
            return badRequest(login.render(loginForm));
        } else {
            User user = User.find.where().eq("email", loginForm.get().email).findUnique();
            Users.setUpSession(user);
            user.save();
            return redirect(
                routes.Application.index()
            );
        }
    }
        
    public static Result accountRecover() {
    	return ok(views.html.accountRecover.render(form(RecoverAccount.class)));
    }
    
    public static Result resetPassword(String resetToken) {
    	User user = User.find.where().eq("reset_token", resetToken).findUnique();
		//token only valid if exists in database and is not expired
		if (user != null && !user.isResetTokenExpired()) {
			return ok(views.html.resetPassword.render(user, form(ResetPassword.class)));
		} else {
			flash("message", "Reset link has expired, please try again");
			return redirect(routes.Application.accountRecover());
		}
    }
    
    public static Result javascriptRoutes() {
    	response().setContentType("text/javascript");
    	return ok(
    		Routes.javascriptRouter("jsRoutes", 
				controllers.routes.javascript.LifeStories.createChapter(),
				controllers.routes.javascript.LifeStories.createPage(),
				controllers.routes.javascript.LifeStories.createItem(),
				controllers.routes.javascript.LifeStories.getChapter(),
				controllers.routes.javascript.LifeStories.getPage(),
				controllers.routes.javascript.LifeStories.getItem(),
				controllers.routes.javascript.LifeStories.editChapter(),
				controllers.routes.javascript.LifeStories.editPage(),
				controllers.routes.javascript.LifeStories.editItem(),
				controllers.routes.javascript.LifeStories.deleteChapter(),
				controllers.routes.javascript.LifeStories.deletePage(),
				controllers.routes.javascript.LifeStories.deleteItem(),
				controllers.routes.javascript.LifeStories.getMyStory(),
				controllers.routes.javascript.LifeStories.getAllMyStory(),
				controllers.routes.javascript.Pictures.getPictureInfo(),
				controllers.routes.javascript.Pictures.uploadPicture()
    		)	
    	);
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
    
    public static class ResetPassword {
		
		@Required
		@MaxLength(64)
		@MinLength(64)
		public String passwordHash;
	}
}
