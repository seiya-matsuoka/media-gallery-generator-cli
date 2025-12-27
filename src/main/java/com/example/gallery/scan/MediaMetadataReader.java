package com.example.gallery.scan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;

/** メディアファイルのメタ情報（ファイルサイズ・最終更新日時）を取得する。 */
public final class MediaMetadataReader {

  private MediaMetadataReader() {}

  /**
   * ファイルサイズ（bytes）を取得する。
   *
   * @param file 対象ファイル
   * @return ファイルサイズ（bytes）
   * @throws IOException 取得に失敗した場合
   */
  public static long sizeBytes(Path file) throws IOException {
    return Files.readAttributes(file, BasicFileAttributes.class).size();
  }

  /**
   * 最終更新日時を取得する。
   *
   * @param file 対象ファイル
   * @return 最終更新日時
   * @throws IOException 取得に失敗した場合
   */
  public static Instant lastModifiedAt(Path file) throws IOException {
    return Files.readAttributes(file, BasicFileAttributes.class).lastModifiedTime().toInstant();
  }

  /**
   * ファイルサイズと最終更新日時をまとめて取得する。
   *
   * @param file 対象ファイル
   * @return メタ情報
   * @throws IOException 取得に失敗した場合
   */
  public static Metadata read(Path file) throws IOException {
    BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
    return new Metadata(attrs.size(), attrs.lastModifiedTime().toInstant());
  }

  /** メタ情報（ファイルサイズ・最終更新日時） */
  public record Metadata(long sizeBytes, Instant lastModifiedAt) {}
}
