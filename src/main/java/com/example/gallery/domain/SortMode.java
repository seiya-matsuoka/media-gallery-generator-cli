package com.example.gallery.domain;

import java.util.Arrays;
import java.util.Locale;

/** 並び順のモードを表す。 */
public enum SortMode {
  /** 更新日時の降順（新しい順）。 */
  MODIFIED_DESC("modified_desc");

  private final String id;

  SortMode(String id) {
    this.id = id;
  }

  /** 設定ファイル上の識別子（例: modified_desc）を返す。 */
  public String id() {
    return id;
  }

  /**
   * 設定値（文字列）から {@link SortMode} を解決する。
   *
   * @param value 設定値（null/空は不可）
   * @return 解決した {@link SortMode}
   * @throws IllegalArgumentException 不正な値の場合
   */
  public static SortMode from(String value) {
    String normalized = value.trim().toLowerCase(Locale.ROOT);
    return Arrays.stream(values())
        .filter(m -> m.id.equals(normalized))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("不正なsortです: " + value));
  }
}
