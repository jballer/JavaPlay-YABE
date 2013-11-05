import org.junit.*;
import java.util.*;
import play.test.*;
import models.*;

public class FullTest extends UnitTest {

	// Delete the database before running db tests
	@Before
    public void setup() {
		// Fixtures is a db helper during JUnit testing
        Fixtures.deleteDatabase();
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
}