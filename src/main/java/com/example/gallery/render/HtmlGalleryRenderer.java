package com.example.gallery.render;

import com.example.gallery.domain.MediaItem;
import com.example.gallery.domain.MediaType;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** メディア一覧からギャラリーHTML（index.html）を生成する。 */
public final class HtmlGalleryRenderer {

  private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");

  private HtmlGalleryRenderer() {}

  /**
   * テンプレHTMLへギャラリー内容を差し込んだHTML文字列を生成する。
   *
   * @param templateHtml テンプレHTML
   * @param title タイトル
   * @param items メディア一覧（assets配下にコピー済みであること）
   * @return 生成HTML
   */
  public static String render(String templateHtml, String title, List<MediaItem> items) {
    String generatedAt = ZonedDateTime.now().format(DT);

    String cards = renderCards(items);

    return templateHtml
        .replace("{{TITLE}}", escapeHtml(title))
        .replace("{{GENERATED_AT}}", escapeHtml(generatedAt))
        .replace("{{ITEMS}}", cards);
  }

  private static String renderCards(List<MediaItem> items) {
    if (items == null || items.isEmpty()) {
      return "<p class=\"meta\" style=\"grid-column: 1 / -1;\">メディアがありません</p>\n";
    }

    StringBuilder sb = new StringBuilder();
    for (MediaItem item : items) {
      String rel = item.relativePath().toString().replace('\\', '/');
      String src = "assets/" + rel;

      sb.append("<figure>\n");
      if (item.type() == MediaType.VIDEO) {
        sb.append("<video controls src=\"").append(escapeHtmlAttr(src)).append("\"></video>\n");
      } else {
        sb.append("<img loading=\"lazy\" src=\"")
            .append(escapeHtmlAttr(src))
            .append("\" alt=\"\" />\n");
      }
      sb.append("<figcaption>").append(escapeHtml(rel)).append("</figcaption>\n");
      sb.append("</figure>\n");
    }
    return sb.toString();
  }

  private static String escapeHtml(String s) {
    return s == null
        ? ""
        : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
  }

  private static String escapeHtmlAttr(String s) {
    // 属性値用
    return escapeHtml(s);
  }
}
