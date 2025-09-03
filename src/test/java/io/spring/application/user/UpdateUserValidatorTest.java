package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UpdateUserValidatorTest {

  @Autowired private UserRepository userRepository;
  @Autowired private UserService userService;

  private User targetUser;
  private User anotherUser;

  @BeforeEach
  public void setUp() {
    targetUser = new User("target@example.com", "targetuser", "password", "bio", "image");
    userRepository.save(targetUser);

    anotherUser = new User("another@example.com", "anotheruser", "password", "bio", "image");
    userRepository.save(anotherUser);
  }

  @Test
  public void should_update_user_successfully_with_unique_email_and_username() {
    UpdateUserParam param =
        UpdateUserParam.builder().email("unique@example.com").username("uniqueuser").build();

    UpdateUserCommand command = new UpdateUserCommand(targetUser, param);

    userService.updateUser(command);
  }

  @Test
  public void should_update_user_when_keeping_same_email() {
    UpdateUserParam param =
        UpdateUserParam.builder().email(targetUser.getEmail()).username("newusername").build();

    UpdateUserCommand command = new UpdateUserCommand(targetUser, param);

    userService.updateUser(command);
  }

  @Test
  public void should_update_user_when_keeping_same_username() {
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("newemail@example.com")
            .username(targetUser.getUsername())
            .build();

    UpdateUserCommand command = new UpdateUserCommand(targetUser, param);

    userService.updateUser(command);
  }

  @Test
  public void should_update_user_when_keeping_same_email_and_username() {
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email(targetUser.getEmail())
            .username(targetUser.getUsername())
            .bio("Updated bio")
            .build();

    UpdateUserCommand command = new UpdateUserCommand(targetUser, param);

    userService.updateUser(command);
  }

  @Test
  public void should_throw_validation_exception_when_email_already_exists() {
    UpdateUserParam param =
        UpdateUserParam.builder().email(anotherUser.getEmail()).username("uniqueuser").build();

    UpdateUserCommand command = new UpdateUserCommand(targetUser, param);

    assertThrows(ConstraintViolationException.class, () -> userService.updateUser(command));
  }

  @Test
  public void should_throw_validation_exception_when_username_already_exists() {
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("unique@example.com")
            .username(anotherUser.getUsername())
            .build();

    UpdateUserCommand command = new UpdateUserCommand(targetUser, param);

    assertThrows(ConstraintViolationException.class, () -> userService.updateUser(command));
  }

  @Test
  public void should_throw_validation_exception_when_both_email_and_username_already_exist() {
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email(anotherUser.getEmail())
            .username(anotherUser.getUsername())
            .build();

    UpdateUserCommand command = new UpdateUserCommand(targetUser, param);

    assertThrows(ConstraintViolationException.class, () -> userService.updateUser(command));
  }

  @Test
  public void should_update_user_when_only_updating_bio_and_image() {
    UpdateUserParam param =
        UpdateUserParam.builder().bio("Updated bio").image("updated-image.jpg").build();

    UpdateUserCommand command = new UpdateUserCommand(targetUser, param);

    userService.updateUser(command);
  }
}
