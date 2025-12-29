package com.example.gallery.render;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.gallery.domain.MediaItem;
import com.example.gallery.domain.MediaType;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class HtmlGalleryRendererTest {

  @Test
  // テンプレのプレースホルダが置換され、img/videoカードが生成されることを確認する
  void rendersTemplateAndCards() {
    String template =
        """
        <html>
          <head><title>{{TITLE}}</title></head>
          <body>
            <div class="meta">{{GENERATED_AT}}</div>
            <div class="grid">{{ITEMS}}</div>
          </body>
        </html>
        """;

    List<MediaItem> items =
        List.of(
            new MediaItem(
                Path.of("work/media/a.jpg"),
                Path.of("a.jpg"),
                MediaType.IMAGE,
                1L,
                Instant.parse("2025-01-01T00:00:00Z")),
            new MediaItem(
                Path.of("work/media/movies/b.mp4"),
                Path.of("movies/b.mp4"),
                MediaType.VIDEO,
                2L,
                Instant.parse("2025-01-02T00:00:00Z")));

    String html = HtmlGalleryRenderer.render(template, "Media Gallery", items);

    // TITLE が差し込まれている
    assertTrue(html.contains("<title>Media Gallery</title>"));

    // GENERATED_AT が置換されており、プレースホルダが残っていない
    assertFalse(html.contains("{{GENERATED_AT}}"));

    // 画像は img、動画は video で出力される
    assertTrue(html.contains("<img"));
    assertTrue(html.contains("src=\"assets/a.jpg\""));

    assertTrue(html.contains("<video"));
    assertTrue(html.contains("src=\"assets/movies/b.mp4\""));
  }

  // タイトルやファイル名に特殊文字が含まれてもHTMLが壊れないようエスケープされることを確認する
  @Test
  void escapesTitleAndPaths() {
    String template = "<h1>{{TITLE}}</h1><div>{{ITEMS}}</div>";

    List<MediaItem> items =
        List.of(
            new MediaItem(
                Path.of("work/media/a&b.jpg"),
                Path.of("a&b.jpg"),
                MediaType.IMAGE,
                1L,
                Instant.parse("2025-01-01T00:00:00Z")));

    String html = HtmlGalleryRenderer.render(template, "A&B <Title>", items);

    // タイトルのエスケープ（< > & を検証）
    assertTrue(html.contains("A&amp;B &lt;Title&gt;"));

    // パス（figcaption）と src 属性のエスケープ（& を検証）
    assertTrue(html.contains("assets/a&amp;b.jpg"));
    assertTrue(html.contains("a&amp;b.jpg"));
  }

  // items が0件のとき、空状態のメッセージが出力されることを確認する
  @Test
  void rendersEmptyMessageWhenNoItems() {
    String template = "<div class=\"grid\">{{ITEMS}}</div>";
    String html = HtmlGalleryRenderer.render(template, "Title", List.of());

    assertTrue(html.contains("メディアがありません"));
    assertFalse(html.contains("{{ITEMS}}"));
  }
}
