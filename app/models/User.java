package models;

import javax.persistence.*;

import org.mindrot.jbcrypt.BCrypt;

import play.db.jpa.*;
import play.data.validation.*;

@Entity
@Table(name="blog_user")
public class User extends Model {
	
	@Email
	@Required
	public String email;
	
	@Required
	public String password;
	public String fullname;
	public boolean isAdmin;
	
	public User(String email, String password, String fullname) {
		this.email = email;
		this.password = password;
		this.fullname = fullname;
	}
	
	public void setPassword(String cleartext) {
		password = BCrypt.hashpw(cleartext, BCrypt.gensalt());
	}
	
	public boolean checkPassword(String cleartext) {
		return BCrypt.checkpw(cleartext, password);
	}
	
	public static User connect(String email, String password) {
		User user = find("byEmail", email.toLowerCase()).first();
		if(user != null && user.checkPassword(password)) {
			return  user;
		}
		else {
			return null;
		}
	}
	
	@Override
	public String toString() {
		return email;
	}
}