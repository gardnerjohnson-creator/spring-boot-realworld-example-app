package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void should_create_user_with_all_fields() {
    String email = "test@example.com";
    String username = "testuser";
    String password = "password123";
    String bio = "Test bio";
    String image = "test-image.jpg";

    User user = new User(email, username, password, bio, image);

    assertEquals(email, user.getEmail());
    assertEquals(username, user.getUsername());
    assertEquals(password, user.getPassword());
    assertEquals(bio, user.getBio());
    assertEquals(image, user.getImage());
    assertNotNull(user.getId());
  }

  @Test
  public void should_create_user_with_empty_bio_and_image() {
    String email = "test@example.com";
    String username = "testuser";
    String password = "password123";

    User user = new User(email, username, password, "", "");

    assertEquals(email, user.getEmail());
    assertEquals(username, user.getUsername());
    assertEquals(password, user.getPassword());
    assertEquals("", user.getBio());
    assertEquals("", user.getImage());
    assertNotNull(user.getId());
  }

  @Test
  public void should_update_email() {
    User user = new User("original@example.com", "username", "password", "bio", "image");
    String newEmail = "updated@example.com";

    user.update(newEmail, null, null, null, null);

    assertEquals(newEmail, user.getEmail());
    assertEquals("username", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image", user.getImage());
  }

  @Test
  public void should_update_username() {
    User user = new User("email@example.com", "original", "password", "bio", "image");
    String newUsername = "updated";

    user.update(null, newUsername, null, null, null);

    assertEquals("email@example.com", user.getEmail());
    assertEquals(newUsername, user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image", user.getImage());
  }

  @Test
  public void should_update_password() {
    User user = new User("email@example.com", "username", "original", "bio", "image");
    String newPassword = "updated";

    user.update(null, null, newPassword, null, null);

    assertEquals("email@example.com", user.getEmail());
    assertEquals("username", user.getUsername());
    assertEquals(newPassword, user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image", user.getImage());
  }

  @Test
  public void should_update_bio() {
    User user = new User("email@example.com", "username", "password", "original", "image");
    String newBio = "updated bio";

    user.update(null, null, null, newBio, null);

    assertEquals("email@example.com", user.getEmail());
    assertEquals("username", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals(newBio, user.getBio());
    assertEquals("image", user.getImage());
  }

  @Test
  public void should_update_image() {
    User user = new User("email@example.com", "username", "password", "bio", "original.jpg");
    String newImage = "updated.jpg";

    user.update(null, null, null, null, newImage);

    assertEquals("email@example.com", user.getEmail());
    assertEquals("username", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals(newImage, user.getImage());
  }

  @Test
  public void should_update_multiple_fields() {
    User user =
        new User("original@example.com", "original", "original", "original", "original.jpg");

    user.update("updated@example.com", "updated", "updated", "updated bio", "updated.jpg");

    assertEquals("updated@example.com", user.getEmail());
    assertEquals("updated", user.getUsername());
    assertEquals("updated", user.getPassword());
    assertEquals("updated bio", user.getBio());
    assertEquals("updated.jpg", user.getImage());
  }

  @Test
  public void should_not_update_with_empty_values() {
    User user = new User("email@example.com", "username", "password", "bio", "image.jpg");

    user.update("", "", "", "", "");

    assertEquals("email@example.com", user.getEmail());
    assertEquals("username", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image.jpg", user.getImage());
  }

  @Test
  public void should_not_update_with_null_values() {
    User user = new User("email@example.com", "username", "password", "bio", "image.jpg");

    user.update(null, null, null, null, null);

    assertEquals("email@example.com", user.getEmail());
    assertEquals("username", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image.jpg", user.getImage());
  }

  @Test
  public void should_update_only_non_empty_fields() {
    User user =
        new User("original@example.com", "original", "original", "original", "original.jpg");

    user.update("updated@example.com", "", "updated", "", "updated.jpg");

    assertEquals("updated@example.com", user.getEmail());
    assertEquals("original", user.getUsername());
    assertEquals("updated", user.getPassword());
    assertEquals("original", user.getBio());
    assertEquals("updated.jpg", user.getImage());
  }
}
