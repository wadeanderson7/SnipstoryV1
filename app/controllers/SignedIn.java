package controllers;

import play.mvc.Result;
import play.mvc.Http.Context;
import play.mvc.Security.Authenticator;

public class SignedIn extends Authenticator {
	@Override
    public String getUsername(Context ctx) {
        return ctx.session().get("uid");
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(routes.Application.login());
    }
}
