package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import graphql.ExecutionResult;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.ProfileQueryService;
import io.spring.application.TagsQueryService;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class GraphQLIntegrationTest {

  @Autowired private DgsQueryExecutor dgsQueryExecutor;

  @MockBean private ArticleQueryService articleQueryService;
  @MockBean private UserQueryService userQueryService;
  @MockBean private ProfileQueryService profileQueryService;
  @MockBean private TagsQueryService tagsQueryService;
  @MockBean private UserRepository userRepository;
  @MockBean private UserReadService userReadService;
  @MockBean private JwtService jwtService;

  private User testUser;
  private UserData testUserData;
  private String token;

  @BeforeEach
  public void setUp() {
    String email = "john@jacob.com";
    String username = "johnjacob";
    String defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";

    testUser = new User(email, username, "123", "", defaultAvatar);
    when(userRepository.findByUsername(eq(username))).thenReturn(Optional.of(testUser));
    when(userRepository.findById(eq(testUser.getId()))).thenReturn(Optional.of(testUser));

    testUserData = new UserData(testUser.getId(), email, username, "", defaultAvatar);
    when(userReadService.findById(eq(testUser.getId()))).thenReturn(testUserData);

    token = "token";
    when(jwtService.getSubFromToken(eq(token))).thenReturn(Optional.of(testUser.getId()));

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(testUser, null, null);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @Test
  public void should_query_me_requires_authorization() {
    String query = "query { me { email username profile { username bio } } }";

    ExecutionResult result = dgsQueryExecutor.execute(query);

    assertFalse(result.getErrors().isEmpty());
    assertTrue(
        result
            .getErrors()
            .get(0)
            .getMessage()
            .contains("Required header 'Authorization' was not provided"));
  }

  @Test
  public void should_query_tags_success() {
    String query = "query { tags }";

    List<String> tags = Arrays.asList("java", "spring", "graphql");
    when(tagsQueryService.allTags()).thenReturn(tags);

    ExecutionResult result = dgsQueryExecutor.execute(query);
    assertTrue(result.getErrors().isEmpty());
    assertNotNull(result.getData());
  }

  @Test
  public void should_query_articles_with_pagination() {
    String query =
        "query { articles(first: 10) { edges { node { title slug } } pageInfo { hasNextPage hasPreviousPage } } }";

    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(new CursorPager<>(Collections.emptyList(), Direction.NEXT, false));

    ExecutionResult result = dgsQueryExecutor.execute(query);
    assertTrue(result.getErrors().isEmpty());
    assertNotNull(result.getData());
  }

  @Test
  public void should_query_article_by_slug() {
    String query =
        "query { article(slug: \"test-article\") { title body favorited favoritesCount } }";

    when(articleQueryService.findBySlug(eq("test-article"), any())).thenReturn(Optional.empty());

    ExecutionResult result = dgsQueryExecutor.execute(query);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  public void should_query_profile_by_username() {
    String query =
        "query { profile(username: \"testuser\") { profile { username bio following } } }";

    when(profileQueryService.findByUsername(eq("testuser"), any())).thenReturn(Optional.empty());

    ExecutionResult result = dgsQueryExecutor.execute(query);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  public void should_query_feed_returns_empty_without_authentication() {
    String query = "query { feed(first: 5) { edges { node { title } } pageInfo { hasNextPage } } }";

    when(articleQueryService.findUserFeedWithCursor(any(), any()))
        .thenReturn(new CursorPager<>(Collections.emptyList(), Direction.NEXT, false));

    ExecutionResult result = dgsQueryExecutor.execute(query);
    assertTrue(result.getErrors().isEmpty());
    assertNotNull(result.getData());
  }
}
