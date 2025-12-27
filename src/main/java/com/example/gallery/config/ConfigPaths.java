package com.example.gallery.config;

import java.nio.file.Path;

/** 設定ファイルを基準としたパス解決ユーティリティ。 */
public final class ConfigPaths {

  private ConfigPaths() {}

  /**
   * 設定ファイルの「基準ディレクトリ」（configファイルの親）を返します。
   *
   * @param configPath 設定ファイルのパス
   * @return 基準ディレクトリ
   */
  public static Path baseDir(Path configPath) {
    Path abs = configPath.toAbsolutePath().normalize();
    Path parent = abs.getParent();
    return parent != null ? parent : Path.of(".").toAbsolutePath().normalize();
  }

  /**
   * 基準ディレクトリに対して相対/絶対パスを解決する。
   *
   * <ul>
   *   <li>絶対パス：そのまま正規化して返す
   *   <li>相対パス：baseDirにresolveして返す
   * </ul>
   *
   * @param baseDir 基準ディレクトリ
   * @param value 解決対象（文字列）
   * @return 解決済みPath
   */
  public static Path resolveAgainstBase(Path baseDir, String value) {
    Path p = Path.of(value);
    if (p.isAbsolute()) {
      return p.normalize();
    }
    return baseDir.resolve(p).normalize();
  }

  /**
   * 設定ファイルの場所を基準に、相対/絶対パスを解決する。
   *
   * @param configPath 設定ファイルのパス
   * @param value 解決対象（文字列）
   * @return 解決済みPath
   */
  public static Path resolveAgainstConfigDir(Path configPath, String value) {
    return resolveAgainstBase(baseDir(configPath), value);
  }
}
