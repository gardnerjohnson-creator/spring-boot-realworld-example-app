package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleTest {

  @Test
  public void should_get_right_slug() {
    Article article = new Article("a new   title", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("a-new-title"));
  }

  @Test
  public void should_get_right_slug_with_number_in_title() {
    Article article = new Article("a new title 2", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("a-new-title-2"));
  }

  @Test
  public void should_get_lower_case_slug() {
    Article article = new Article("A NEW TITLE", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("a-new-title"));
  }

  @Test
  public void should_handle_other_language() {
    Article article = new Article("中文：标题", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("中文-标题"));
  }

  @Test
  public void should_handle_commas() {
    Article article = new Article("what?the.hell,w", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("what-the-hell-w"));
  }

  @Test
  public void should_generate_slug_with_special_characters() {
    String title = "Spring Boot & GraphQL: A Complete Guide!";
    String expectedSlug = "spring-boot-graphql:-a-complete-guide!";

    String actualSlug = Article.toSlug(title);

    assertEquals(expectedSlug, actualSlug);
  }

  @Test
  public void should_generate_slug_with_unicode_characters() {
    String title = "学习Spring Boot框架";
    String expectedSlug = "学习spring-boot框架";

    String actualSlug = Article.toSlug(title);

    assertEquals(expectedSlug, actualSlug);
  }

  @Test
  public void should_generate_slug_with_punctuation() {
    String title = "What's new in Java 17? Let's explore!";
    String expectedSlug = "what's-new-in-java-17-let's-explore!";

    String actualSlug = Article.toSlug(title);

    assertEquals(expectedSlug, actualSlug);
  }

  @Test
  public void should_create_article_with_generated_slug() {
    String title = "Test Article Title";
    List<String> tags = Arrays.asList("java", "testing");
    String userId = "user123";

    Article article = new Article(title, "Description", "Body", tags, userId);

    assertEquals("test-article-title", article.getSlug());
    assertEquals(title, article.getTitle());
    assertEquals(userId, article.getUserId());
    assertNotNull(article.getId());
    assertNotNull(article.getCreatedAt());
    assertNotNull(article.getUpdatedAt());
    assertEquals(2, article.getTags().size());
  }

  @Test
  public void should_update_title_and_regenerate_slug() throws InterruptedException {
    Article article =
        new Article("Original Title", "Description", "Body", Arrays.asList(), "user123");
    DateTime originalUpdatedAt = article.getUpdatedAt();

    Thread.sleep(1);
    article.update("Updated Title", null, null);

    assertEquals("Updated Title", article.getTitle());
    assertEquals("updated-title", article.getSlug());
    assertTrue(article.getUpdatedAt().isAfter(originalUpdatedAt));
  }

  @Test
  public void should_update_description() throws InterruptedException {
    Article article =
        new Article("Title", "Original Description", "Body", Arrays.asList(), "user123");
    DateTime originalUpdatedAt = article.getUpdatedAt();

    Thread.sleep(1);
    article.update(null, "Updated Description", null);

    assertEquals("Title", article.getTitle());
    assertEquals("Updated Description", article.getDescription());
    assertTrue(article.getUpdatedAt().isAfter(originalUpdatedAt));
  }

  @Test
  public void should_not_update_with_empty_values() {
    Article article = new Article("Title", "Description", "Body", Arrays.asList(), "user123");
    DateTime originalUpdatedAt = article.getUpdatedAt();

    article.update("", "", "");

    assertEquals("Title", article.getTitle());
    assertEquals("Description", article.getDescription());
    assertEquals("Body", article.getBody());
    assertEquals(originalUpdatedAt, article.getUpdatedAt());
  }

  @Test
  public void should_remove_duplicate_tags() {
    List<String> tagsWithDuplicates = Arrays.asList("java", "spring", "java", "testing", "spring");

    Article article = new Article("Title", "Description", "Body", tagsWithDuplicates, "user123");

    assertEquals(3, article.getTags().size());
  }
}
