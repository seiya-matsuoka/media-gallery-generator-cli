package com.example.gallery.render;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** resources 配下のHTMLテンプレートを読み込む。 */
public final class HtmlTemplateLoader {

  private HtmlTemplateLoader() {}

  /**
   * クラスパス上のテンプレートをUTF-8で読み込む。
   *
   * @param resourcePath 例: "/templates/index.html"
   * @return テンプレート文字列
   * @throws IOException 読み込みに失敗した場合
   */
  public static String loadUtf8(String resourcePath) throws IOException {
    try (InputStream in = HtmlTemplateLoader.class.getResourceAsStream(resourcePath)) {
      if (in == null) {
        throw new IOException("テンプレートが見つかりません: " + resourcePath);
      }
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
