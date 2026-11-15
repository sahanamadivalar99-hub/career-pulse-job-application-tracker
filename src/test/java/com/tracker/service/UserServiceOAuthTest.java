package com.tracker.service;

import com.tracker.model.User;
import com.tracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserServiceOAuthTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testExistingUserReturnedWithoutDuplicate() {
        // Arrange
        String email = "existing.oauth.test@example.com";
        User user1 = userService.processOAuthPostLogin(email, "Initial Name", "https://example.com/pic1.jpg");
        assertNotNull(user1.getId());
        long originalCount = userRepository.count();

        // Act
        User user2 = userService.processOAuthPostLogin(email, "Updated Name", "https://example.com/pic2.jpg");

        // Assert
        assertEquals(user1.getId(), user2.getId());
        assertEquals(originalCount, userRepository.count());
        assertEquals("https://example.com/pic2.jpg", user2.getProfilePicture());
    }

    @Test
    void testNullNameAndPictureFallback() {
        // Arrange
        String email = "nullname.test@example.com";

        // Act
        User user = userService.processOAuthPostLogin(email, null, null);

        // Assert
        assertNotNull(user);
        assertEquals("nullname.test", user.getFullName());
        assertNull(user.getProfilePicture());
        assertNotNull(user.getPassword());
        assertTrue(user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("{bcrypt}"));
    }

    @Test
    void testLongAvatarUrlTruncation() {
        // Arrange
        String email = "longavatar.test@example.com";
        String veryLongUrl = "https://lh3.googleusercontent.com/a/" + "a".repeat(1200);

        // Act
        User user = userService.processOAuthPostLogin(email, "Long Avatar User", veryLongUrl);

        // Assert
        assertNotNull(user);
        assertNotNull(user.getProfilePicture());
        assertTrue(user.getProfilePicture().length() <= 1000);
    }

    @Test
    void testUniqueUsernameGenerationOnCollision() {
        // Arrange
        String email1 = "collision@example.com";
        String email2 = "collision@otherdomain.com";

        // Act
        User user1 = userService.processOAuthPostLogin(email1, "First User", null);
        User user2 = userService.processOAuthPostLogin(email2, "Second User", null);

        // Assert
        assertNotNull(user1);
        assertNotNull(user2);
        assertNotEquals(user1.getUsername(), user2.getUsername());
        assertEquals("collision", user1.getUsername());
        assertEquals("collision1", user2.getUsername());
    }
}
