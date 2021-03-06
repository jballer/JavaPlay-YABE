package controllers;

import play.db.jpa.GenericModel;
import models.*;

public class Security extends Secure.Security {

	static boolean authenticate(String username, String password) {
		return User.connect(username, password) != null;
	}
	
	static void onDisconnected() {
		Application.index();
	}
	
//	static void onAuthenticated() {
//		Application.index();
//	}
	
	static boolean check(String profile) {
		if("admin".equals(profile)) {
			User user = User.find("byEmail", connected()).first();
			return user.isAdmin;
		}
		
		return false;
	}
}