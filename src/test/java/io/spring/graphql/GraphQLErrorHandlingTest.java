package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UserService;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import java.util.Set;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class GraphQLErrorHandlingTest {

  @Autowired private DgsQueryExecutor dgsQueryExecutor;

  @MockBean private UserService userService;
  @MockBean private UserRepository userRepository;
  @MockBean private PasswordEncoder passwordEncoder;

  @Test
  public void should_handle_invalid_authentication_exception() {
    String mutation =
        "mutation { "
            + "login(email: \"nonexistent@example.com\", password: \"password\") { "
            + "user { email username } } }";

    when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

    ExecutionResult result = dgsQueryExecutor.execute(mutation);
    assertFalse(result.getErrors().isEmpty());

    GraphQLError error = result.getErrors().get(0);
    assertTrue(error.getMessage().contains("invalid email or password"));
  }

  @Test
  public void should_handle_constraint_violation_exception() {
    String mutation =
        "mutation { "
            + "createUser(input: { email: \"invalid-email\", username: \"testuser\", password: \"password123\" }) { "
            + "... on UserPayload { user { email username } } "
            + "... on Error { message errors { key value } } } }";

    ConstraintViolationException cve =
        new ConstraintViolationException("Validation failed", Set.of());
    when(userService.createUser(any(RegisterParam.class))).thenThrow(cve);

    ExecutionResult result = dgsQueryExecutor.execute(mutation);

    assertNotNull(result.getData());
    assertTrue(result.getErrors().isEmpty());
  }

  @Test
  public void should_handle_malformed_graphql_query() {
    String malformedQuery = "query { invalidField { nonExistentField } }";

    ExecutionResult result = dgsQueryExecutor.execute(malformedQuery);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  public void should_handle_missing_required_arguments() {
    String mutation =
        "mutation { "
            + "createUser(input: { username: \"testuser\" }) { "
            + "... on UserPayload { user { email username } } } }";

    ExecutionResult result = dgsQueryExecutor.execute(mutation);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  public void should_handle_authentication_required_operations() {
    String mutation =
        "mutation { "
            + "updateUser(changes: { bio: \"New bio\" }) { "
            + "user { username profile { bio } } } }";

    ExecutionResult result = dgsQueryExecutor.execute(mutation);
    assertTrue(result.getData() == null || result.getErrors().size() > 0);
  }

  @Test
  public void should_handle_invalid_input_types() {
    String mutation =
        "mutation { " + "login(email: 123, password: \"password\") { " + "user { email } } }";

    ExecutionResult result = dgsQueryExecutor.execute(mutation);
    assertFalse(result.getErrors().isEmpty());
  }
}
