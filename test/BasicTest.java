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
	
	@Test
	public void fullTest() {
		
		// Load data from YML
		Fixtures.loadModels("data.yml");
		
		// Count things
		assertEquals(2, User.count());
		assertEquals(3, Post.count());
		assertEquals(3, Comment.count());
		
		// Try to connect as some of the users
		assertNotNull(User.connect("bob@gmail.com", "secret"));
		assertNotNull(User.connect("jeff@gmail.com", "secret"));
		assertNull(User.connect("jeff@gmail.com", "badpassword"));
		assertNull(User.connect("tom@gmail.com", "secret"));
		
		// Find Bob's posts
		List<Post> bobPosts = Post.find("author.email", "bob@gmail.com").fetch();
		assertEquals(2, bobPosts.size());
		
		// Find comments related to all of Bob's posts
		List<Comment> bobComments = Comment.find("post.author.email", "bob@gmail.com").fetch();
		assertEquals(3, bobComments.size());
		
		// Find the most recent post
		Post frontPost = Post.find("order by postedAt desc").first();
		assertNotNull(frontPost);
		assertEquals("About the model layer", frontPost.title);
		
		// Check that the post has two comments
		assertEquals(2, frontPost.comments.size());
		
		// Post and check a new comment
		frontPost.addComment("Jim", "Hello there");
		assertEquals(3, frontPost.comments.size());
		assertEquals(4, Comment.count());
	}
	
	@Test
	public void testTags() {
		// create a user
		User bob = new User("bob@gmail.com", "secret", "Bob").save();
		
		// create a post
		Post post = new Post(bob, "this", "post").save();
		Post anotherPost = new Post(bob, "that", "other post").save();
		
		// make sure the tag hasn't been used yet
		assertEquals(0, Post.findTaggedWith("Red").size());
		
		// tag this post with the new tag
		post.tagItWith("Red").tagItWith("Blue").save();
		anotherPost.tagItWith("Red").tagItWith("Green").save();
		
		// check the tags
		assertEquals(2, Post.findTaggedWith("Red").size());
		assertEquals(1, Post.findTaggedWith("Blue").size());
		assertEquals(1, Post.findTaggedWith("Green").size());
		
		// Now test retrieval based on multiple tags
		assertEquals(1, Post.findTaggedWith("Red", "Blue").size());
		assertEquals(1, Post.findTaggedWith("Red", "Green").size());
		assertEquals(0, Post.findTaggedWith("Red", "Green", "Blue").size());
		assertEquals(0, Post.findTaggedWith("Green", "Blue").size());
		
		// Now test that we can get a Tag Cloud
		List<Map> cloud = Tag.getCloud();
		assertEquals("[{tag=Blue, pound=1}, {tag=Green, pound=1}, {tag=Red, pound=2}]", cloud.toString());
	}

}
