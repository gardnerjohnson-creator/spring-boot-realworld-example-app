package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

@Import({UserService.class, MyBatisUserRepository.class})
public class UserServiceTest extends DbTestBase {

  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;

  @MockBean private PasswordEncoder passwordEncoder;

  private User existingUser;

  @BeforeEach
  public void setUp() {
    existingUser = new User("existing@example.com", "existinguser", "password", "bio", "image");
    userRepository.save(existingUser);

    when(passwordEncoder.encode(any(String.class))).thenReturn("encoded-password");
  }

  @Test
  public void should_create_user_success() {
    RegisterParam registerParam = new RegisterParam("test@example.com", "testuser", "password123");

    User createdUser = userService.createUser(registerParam);

    assertNotNull(createdUser);
    assertEquals("test@example.com", createdUser.getEmail());
    assertEquals("testuser", createdUser.getUsername());
    assertEquals("encoded-password", createdUser.getPassword());

    verify(passwordEncoder).encode("password123");
  }

  @Test
  public void should_create_user_with_default_image() {
    RegisterParam registerParam = new RegisterParam("test@example.com", "testuser", "password123");

    User createdUser = userService.createUser(registerParam);

    assertNotNull(createdUser.getImage());
  }

  @Test
  public void should_update_user_success() {
    UpdateUserParam updateParam =
        UpdateUserParam.builder()
            .email("updated@example.com")
            .username("updateduser")
            .bio("Updated bio")
            .image("updated-image.jpg")
            .build();

    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    User updatedUser = userRepository.findById(existingUser.getId()).get();
    assertEquals("updated@example.com", updatedUser.getEmail());
    assertEquals("updateduser", updatedUser.getUsername());
    assertEquals("Updated bio", updatedUser.getBio());
    assertEquals("updated-image.jpg", updatedUser.getImage());
  }

  @Test
  public void should_update_user_partial_fields() {
    UpdateUserParam updateParam = UpdateUserParam.builder().bio("Only bio updated").build();

    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    User updatedUser = userRepository.findById(existingUser.getId()).get();
    assertEquals(existingUser.getEmail(), updatedUser.getEmail());
    assertEquals(existingUser.getUsername(), updatedUser.getUsername());
    assertEquals("Only bio updated", updatedUser.getBio());
  }

  @Test
  public void should_update_user_with_password_encoding() {
    UpdateUserParam updateParam = UpdateUserParam.builder().password("newpassword").build();

    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    User updatedUser = userRepository.findById(existingUser.getId()).get();
    assertEquals("newpassword", updatedUser.getPassword());
  }

  @Test
  public void should_create_user_with_unique_email_and_username() {
    RegisterParam registerParam =
        new RegisterParam("unique@example.com", "uniqueuser", "password123");

    User createdUser = userService.createUser(registerParam);

    assertNotNull(createdUser);
    assertEquals("unique@example.com", createdUser.getEmail());
    assertEquals("uniqueuser", createdUser.getUsername());
  }

  @Test
  public void should_allow_user_to_keep_same_email_on_update() {
    UpdateUserParam updateParam =
        UpdateUserParam.builder().email(existingUser.getEmail()).bio("Updated bio").build();

    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    User updatedUser = userRepository.findById(existingUser.getId()).get();
    assertEquals(existingUser.getEmail(), updatedUser.getEmail());
    assertEquals("Updated bio", updatedUser.getBio());
  }

  @Test
  public void should_allow_user_to_keep_same_username_on_update() {
    UpdateUserParam updateParam =
        UpdateUserParam.builder().username(existingUser.getUsername()).bio("Updated bio").build();

    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    User updatedUser = userRepository.findById(existingUser.getId()).get();
    assertEquals(existingUser.getUsername(), updatedUser.getUsername());
    assertEquals("Updated bio", updatedUser.getBio());
  }
}
