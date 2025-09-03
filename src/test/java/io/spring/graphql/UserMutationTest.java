package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import graphql.ExecutionResult;
import io.spring.application.data.UserData;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UpdateUserCommand;
import io.spring.application.user.UserService;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Optional;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class UserMutationTest {

  @Autowired private DgsQueryExecutor dgsQueryExecutor;

  @MockBean private UserService userService;
  @MockBean private UserRepository userRepository;
  @MockBean private UserReadService userReadService;
  @MockBean private JwtService jwtService;
  @MockBean private PasswordEncoder passwordEncoder;

  private User user;
  private UserData userData;

  @BeforeEach
  public void setUp() {
    String email = "john@jacob.com";
    String username = "johnjacob";
    String defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";

    user = new User(email, username, "123", "", defaultAvatar);
    when(userRepository.findByUsername(eq(username))).thenReturn(Optional.of(user));
    when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

    userData = new UserData(user.getId(), email, username, "", defaultAvatar);
    when(userReadService.findById(eq(user.getId()))).thenReturn(userData);

    String token = "token";
    when(jwtService.getSubFromToken(eq(token))).thenReturn(Optional.of(user.getId()));

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(user, null, null);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @Test
  public void should_create_user_success() {
    String mutation =
        "mutation { "
            + "createUser(input: { email: \"test@example.com\", username: \"testuser\", password: \"password123\" }) { "
            + "... on UserPayload { user { email username } } } }";

    User newUser = new User("test@example.com", "testuser", "encoded", "", "");
    when(userService.createUser(any(RegisterParam.class))).thenReturn(newUser);

    ExecutionResult result = dgsQueryExecutor.execute(mutation);
    assertTrue(result.getErrors().isEmpty());
    assertNotNull(result.getData());
  }

  @Test
  public void should_create_user_with_validation_error() {
    String mutation =
        "mutation { "
            + "createUser(input: { email: \"invalid-email\", username: \"testuser\", password: \"password123\" }) { "
            + "... on Error { message errors { key value } } } }";

    when(userService.createUser(any(RegisterParam.class)))
        .thenThrow(new ConstraintViolationException("Validation failed", null));

    ExecutionResult result = dgsQueryExecutor.execute(mutation);
    assertNotNull(result.getData());
  }

  @Test
  public void should_login_user_success() {
    String mutation =
        "mutation { login(email: \"john@jacob.com\", password: \"password\") { user { email username } } }";

    when(userRepository.findByEmail(eq("john@jacob.com"))).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(eq("password"), eq(user.getPassword()))).thenReturn(true);

    ExecutionResult result = dgsQueryExecutor.execute(mutation);
    assertTrue(result.getErrors().isEmpty());
    assertNotNull(result.getData());
  }

  @Test
  public void should_login_user_with_invalid_credentials() {
    String mutation =
        "mutation { login(email: \"john@jacob.com\", password: \"wrongpassword\") { user { email username } } }";

    when(userRepository.findByEmail(eq("john@jacob.com"))).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(eq("wrongpassword"), eq(user.getPassword()))).thenReturn(false);

    ExecutionResult result = dgsQueryExecutor.execute(mutation);
    assertTrue(result.getErrors().size() > 0);
  }

  @Test
  public void should_update_user_success() {
    String mutation =
        "mutation { updateUser(changes: { email: \"newemail@example.com\", bio: \"Updated bio\" }) { user { email username } } }";

    ExecutionResult result = dgsQueryExecutor.execute(mutation);
    verify(userService).updateUser(any(UpdateUserCommand.class));
  }

  @Test
  public void should_update_user_with_validation_error() {
    String mutation =
        "mutation { updateUser(changes: { email: \"existing@example.com\", username: \"existinguser\" }) { user { email username } } }";

    doThrow(new ConstraintViolationException("Email already exists", null))
        .when(userService)
        .updateUser(any(UpdateUserCommand.class));

    ExecutionResult result = dgsQueryExecutor.execute(mutation);
    assertTrue(result.getErrors().size() > 0);
  }
}
