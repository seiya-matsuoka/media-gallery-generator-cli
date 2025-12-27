package com.example.gallery.scan;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * メディア対象ファイルかどうかを判定するフィルタ。
 *
 * <p>拡張子ベースのフィルタのみを行う。
 */
public final class MediaFilter {

  private MediaFilter() {}

  /**
   * 対象ファイルであれば拡張子（正規化済み）を返す。対象外なら空文字を返す。
   *
   * <p>正規化: 先頭ドット除去 + 小文字化（例: ".JPG" → "jpg"）
   *
   * @param file ファイルパス
   * @param allowedExtensions 許可拡張子（例: "jpg", "png", "mp4"）
   * @return 対象なら正規化済み拡張子、対象外なら空文字
   */
  public static String matchExtension(Path file, Set<String> allowedExtensions) {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(allowedExtensions, "allowedExtensions");

    String ext = extensionOf(file);
    if (ext.isEmpty()) {
      return "";
    }
    String normalized = normalizeExtension(ext);
    if (!allowedExtensions.contains(normalized)) {
      return "";
    }
    return normalized;
  }

  /**
   * 拡張子を正規化する（先頭ドット除去 + 小文字化）。
   *
   * @param ext 拡張子（"jpg" または ".jpg" など）
   * @return 正規化済み拡張子（例: "jpg"）
   */
  public static String normalizeExtension(String ext) {
    String s = ext.trim();
    if (s.startsWith(".")) {
      s = s.substring(1);
    }
    return s.toLowerCase(Locale.ROOT);
  }

  private static String extensionOf(Path file) {
    String name = file.getFileName().toString();
    int idx = name.lastIndexOf('.');
    if (idx < 0 || idx == name.length() - 1) {
      return "";
    }
    return name.substring(idx + 1);
  }
}
