import org.junit.*;
import java.util.*;
import play.test.*;
import models.*;

public class BasicTest extends UnitTest {

	// Delete the database before running db tests
	@Before
    public void setup() {
		// Fixtures is a db helper during JUnit testing
        Fixtures.deleteDatabase();
    }

    @Test
    public void createAndRetrieveUser() {
    	// Create a new user and save it
		new User("bob@gmail.com", "secret", "Bob").save();
		
		// retrieve the new user
		User bob = User.find("byEmail", "bob@gmail.com").first();
		
		// Test
		assertNotNull(bob);
		assertEquals("Bob", bob.fullname);
    }
	
	@Test
	public void tryConnectAsUser() {
		// Create a new user and save it
		new User("bob@gmail.com", "secret", "Bob").save();
		
		// Test
		assertNotNull(User.connect("bob@gmail.com", "secret"));
		assertNull(User.connect("bob@gmail.com", "badpassword"));
		assertNull(User.connect("tom@gmail.com", "secret"));
	}
	
	@Test
	public void createPost() {
		// Create and save a user
		User bob = new User("bob@gmail.com", "secrets", "Bob").save();
		
		// Create a new post
		String postTitle = "My first post";
		String postText = "Hello world";
		new Post(bob, postTitle, postText).save();
		
		// Make sure the post was created
		assertEquals(1, Post.count());
		
		// Retrieve all posts by Bob
		List<Post> bobPosts = Post.find("byAuthor", bob).fetch();
		
		// Tests
		assertEquals(1, bobPosts.size());
		Post firstPost = bobPosts.get(0);
		assertNotNull(firstPost);
		assertEquals(bob, firstPost.author);
		assertEquals(postTitle, firstPost.title);
		assertEquals(postText, firstPost.content);
		assertNotNull(firstPost.postedAt);
	}
	
	@Test
	public void postComments() {
		// Create and save a user
		User bob = new User("bob@gmail.com", "secrets", "Bob").save();
		
		// Create a new post
		Post post = new Post(bob, "My first post", "Hello world").save();
		
		// Add a couple comments to the post
		new Comment(post, "Steve", "first comment!").save();
		new Comment(post, "Ed", "second comment!").save();
		
		// Make sure the comment was created
		assertEquals(2, Comment.count());
		
		// Retrieve the comment
		List<Comment> comments = Comment.find("byPost", post).fetch();
		
		// Test the comments
		assertEquals(2, comments.size());
		
		// First
		Comment theComment = comments.get(0);
		assertNotNull(theComment);
		assertEquals(post, theComment.post);
		assertEquals("Steve", theComment.author);
		assertEquals("first comment!", theComment.content);
		assertNotNull(theComment.postedAt);
		
		// Second
		theComment = comments.get(1);
		assertNotNull(theComment);
		assertEquals(post, theComment.post);
		assertEquals("Ed", theComment.author);
		assertEquals("second comment!", theComment.content);
		assertNotNull(theComment.postedAt);
	}
	
	@Test
	public void useTheCommentsRelation() {
		// Create and save a user
		User bob = new User("bob@gmail.com", "secrets", "Bob").save();
		
		// Create a new post
		Post post = new Post(bob, "My first post", "Hello world").save();
		
		// Add a couple comments to the post
		post.addComment("Steve", "first comment!").save();
		post.addComment("Ed", "second comment!").save();
		
		// Make sure everything's there
		assertEquals(1, User.count());
		assertEquals(1, Post.count());
		assertEquals(2, Comment.count());
		
		// Get the post
		Post thePost = Post.find("byAuthor", bob).first();
		assertNotNull(thePost);
		
		// Get the comments
		Comment theComment = thePost.comments.get(0);
		assertEquals(2, thePost.comments.size());
		assertEquals("Steve", theComment.author);
		assertEquals("first comment!", theComment.content);
		
		// Delete the post
		thePost.delete();
		
		// Ensure the post deletion got rid of the comments too
		assertEquals(1, User.count());
		assertEquals(0, Post.count());
		assertEquals(0, Comment.count());
	}

}
