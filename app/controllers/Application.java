package controllers;

import play.mvc.*;
import play.Play;
import play.data.validation.*;
import play.db.jpa.GenericModel;
import play.libs.*;
import play.cache.*;

import java.util.*;

import models.*;

public class Application extends Controller {

	@Before
	static void addDefaults() {
		renderArgs.put("blogTitle", Play.configuration.getProperty("blog.title"));
		renderArgs.put("blogBaseline", Play.configuration.getProperty("blog.baseline"));
	}

    public static void index() {
		
		// Get and display the list of posts
		Post frontPost = Post.find("order by postedAt desc").first();
		List<Post> olderPosts = Post.find("order by postedAt desc").from(1).fetch(10);
		
        render(frontPost, olderPosts);
    }
	
	public static void show(Long id) {
		Post post = Post.findById(id);
		String randomID = Codec.UUID();
		render(post, randomID);
	}
	
	public static void postComment(
		Long postId, 
		@Required(message="Author is required") String author,
		@Required(message="A message is required") String content,
		@Required(message="Please type the code") String code,
		String randomID) {
		
		Post post = Post.findById(postId);
		// Validate the Captcha
		// System.out.println("ID: " + randomID + "\nEntered: "+code+ "\nExpected: "+ Cache.get(randomID));
		if(!Play.id.equals("test")) {
			validation.equals(
				code.toLowerCase(), ((String)Cache.get(randomID)).toLowerCase()
			).message("Invalid code. Please try again.");
		}
		else {
			System.out.println("WARNING: Running in test mode. CAPTCHA not validated!");
		}
		
		if(validation.hasErrors()) {
			render("Application/show.html", post, randomID, author, content);
		}
		else {
			post.addComment(author, content);
			flash.success("Thanks for posting, %s!", author);
			Cache.delete(randomID);
			show(postId);
		}
	}
	
	public static void captcha(String id) {
		Images.Captcha captcha = Images.captcha();
		String code = captcha.getText("#E4EAFD");
		Cache.set(id, code, "10mn");
		renderBinary(captcha);
	}
	
	public static void listTagged(String tag) {
		List<Post> posts = Post.findTaggedWith(tag);
		render(tag, posts);
	}

}