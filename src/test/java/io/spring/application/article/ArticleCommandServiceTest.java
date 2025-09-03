package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({ArticleCommandService.class, MyBatisArticleRepository.class, MyBatisUserRepository.class})
public class ArticleCommandServiceTest extends DbTestBase {

  @Autowired private ArticleCommandService articleCommandService;
  @Autowired private ArticleRepository articleRepository;
  @Autowired private UserRepository userRepository;

  private User author;

  @BeforeEach
  public void setUp() {
    author = new User("author@example.com", "author", "password", "bio", "image");
    userRepository.save(author);
  }

  @Test
  public void should_create_article_success() {
    List<String> tags = Arrays.asList("java", "spring", "testing");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Article Title")
            .description("This is a test article description")
            .body("This is the body of the test article")
            .tagList(tags)
            .build();

    Article createdArticle = articleCommandService.createArticle(param, author);

    assertNotNull(createdArticle);
    assertEquals("Test Article Title", createdArticle.getTitle());
    assertEquals("This is a test article description", createdArticle.getDescription());
    assertEquals("This is the body of the test article", createdArticle.getBody());
    assertEquals(author.getId(), createdArticle.getUserId());
    assertEquals("test-article-title", createdArticle.getSlug());
    assertEquals(3, createdArticle.getTags().size());
    assertNotNull(createdArticle.getId());
    assertNotNull(createdArticle.getCreatedAt());
    assertNotNull(createdArticle.getUpdatedAt());
  }

  @Test
  public void should_create_article_with_empty_tags() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Article Without Tags")
            .description("Description")
            .body("Body content")
            .tagList(Arrays.asList())
            .build();

    Article createdArticle = articleCommandService.createArticle(param, author);

    assertNotNull(createdArticle);
    assertEquals("Article Without Tags", createdArticle.getTitle());
    assertTrue(createdArticle.getTags().isEmpty());
  }

  @Test
  public void should_create_article_with_duplicate_tags() {
    List<String> tagsWithDuplicates = Arrays.asList("java", "spring", "java", "testing", "spring");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Article With Duplicate Tags")
            .description("Description")
            .body("Body content")
            .tagList(tagsWithDuplicates)
            .build();

    Article createdArticle = articleCommandService.createArticle(param, author);

    assertNotNull(createdArticle);
    assertEquals(3, createdArticle.getTags().size());
  }

  @Test
  public void should_update_article_title_and_slug() {
    Article existingArticle =
        new Article(
            "Original Title",
            "Original description",
            "Original body",
            Arrays.asList("tag1"),
            author.getId());
    articleRepository.save(existingArticle);

    UpdateArticleParam updateParam = new UpdateArticleParam("Updated Title", "", "");

    Article updatedArticle = articleCommandService.updateArticle(existingArticle, updateParam);

    assertEquals("Updated Title", updatedArticle.getTitle());
    assertEquals("updated-title", updatedArticle.getSlug());
    assertEquals("Original description", updatedArticle.getDescription());
    assertEquals("Original body", updatedArticle.getBody());
    assertTrue(updatedArticle.getUpdatedAt().isAfter(updatedArticle.getCreatedAt()));
  }

  @Test
  public void should_update_article_description() {
    Article existingArticle =
        new Article(
            "Title",
            "Original description",
            "Original body",
            Arrays.asList("tag1"),
            author.getId());
    articleRepository.save(existingArticle);

    UpdateArticleParam updateParam = new UpdateArticleParam("", "", "Updated description");

    Article updatedArticle = articleCommandService.updateArticle(existingArticle, updateParam);

    assertEquals("Title", updatedArticle.getTitle());
    assertEquals("Updated description", updatedArticle.getDescription());
    assertEquals("Original body", updatedArticle.getBody());
    assertTrue(updatedArticle.getUpdatedAt().isAfter(updatedArticle.getCreatedAt()));
  }

  @Test
  public void should_update_article_body() {
    Article existingArticle =
        new Article("Title", "Description", "Original body", Arrays.asList("tag1"), author.getId());
    articleRepository.save(existingArticle);

    UpdateArticleParam updateParam = new UpdateArticleParam("", "Updated body content", "");

    Article updatedArticle = articleCommandService.updateArticle(existingArticle, updateParam);

    assertEquals("Title", updatedArticle.getTitle());
    assertEquals("Description", updatedArticle.getDescription());
    assertEquals("Updated body content", updatedArticle.getBody());
    assertTrue(updatedArticle.getUpdatedAt().isAfter(updatedArticle.getCreatedAt()));
  }

  @Test
  public void should_update_article_all_fields() {
    Article existingArticle =
        new Article(
            "Original Title",
            "Original description",
            "Original body",
            Arrays.asList("tag1"),
            author.getId());
    articleRepository.save(existingArticle);

    UpdateArticleParam updateParam =
        new UpdateArticleParam(
            "Completely Updated Title",
            "Completely updated body content",
            "Completely updated description");

    Article updatedArticle = articleCommandService.updateArticle(existingArticle, updateParam);

    assertEquals("Completely Updated Title", updatedArticle.getTitle());
    assertEquals("completely-updated-title", updatedArticle.getSlug());
    assertEquals("Completely updated description", updatedArticle.getDescription());
    assertEquals("Completely updated body content", updatedArticle.getBody());
    assertTrue(updatedArticle.getUpdatedAt().isAfter(updatedArticle.getCreatedAt()));
  }

  @Test
  public void should_not_update_article_with_empty_values() {
    Article existingArticle =
        new Article(
            "Original Title",
            "Original description",
            "Original body",
            Arrays.asList("tag1"),
            author.getId());
    articleRepository.save(existingArticle);

    UpdateArticleParam updateParam = new UpdateArticleParam("", "", "");

    Article updatedArticle = articleCommandService.updateArticle(existingArticle, updateParam);

    assertEquals("Original Title", updatedArticle.getTitle());
    assertEquals("Original description", updatedArticle.getDescription());
    assertEquals("Original body", updatedArticle.getBody());
    assertEquals(updatedArticle.getUpdatedAt(), updatedArticle.getCreatedAt());
  }
}
