package com.example.gallery.scan;

import com.example.gallery.domain.MediaItem;
import com.example.gallery.domain.MediaType;
import com.example.gallery.domain.SortMode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/** inputDir を再帰走査し、対象拡張子のメディア一覧を返す。 */
public final class MediaScanner {

  private MediaScanner() {}

  /**
   * 入力ディレクトリを再帰走査し、対象メディア（画像/動画）の一覧を返す。
   *
   * <ol>
   *   <li>入力ディレクトリの存在・種別（ディレクトリ）・拡張子リストの妥当性を検証
   *   <li>{@link java.nio.file.Files#walk(Path)} により再帰走査
   *   <li>通常ファイルのみを対象に、拡張子フィルタ（{@link MediaFilter}）を適用
   *   <li>対象ファイルのメタ情報（サイズ/更新日時）を取得し、{@link MediaItem} に変換
   *   <li>{@link SortMode} に従ってソート
   * </ol>
   *
   * <p>補足：
   *
   * <ul>
   *   <li>relativePath は inputDir からの相対パスとして保持する
   *   <li>ファイルのメタ情報取得に失敗した場合は、原因が分かるよう例外を送出する
   * </ul>
   *
   * @param inputDir 入力ディレクトリ（再帰走査）
   * @param includeExtensions 対象拡張子（例: jpg, png, mp4）
   * @param sort ソートモード
   * @return メディア一覧（ソート済み）
   * @throws IOException walk中の入出力エラー
   * @throws MediaScanException 入力不正（inputDirが存在しない等）
   */
  public static List<MediaItem> scan(Path inputDir, List<String> includeExtensions, SortMode sort)
      throws IOException {
    Objects.requireNonNull(inputDir, "inputDir");
    Objects.requireNonNull(includeExtensions, "includeExtensions");
    Objects.requireNonNull(sort, "sort");

    Path absInput = inputDir.toAbsolutePath().normalize();
    if (!Files.exists(absInput)) {
      throw new MediaScanException("inputDir が見つかりません: " + absInput);
    }
    if (!Files.isDirectory(absInput)) {
      throw new MediaScanException("inputDir がディレクトリではありません: " + absInput);
    }
    if (includeExtensions.isEmpty()) {
      throw new MediaScanException("includeExtensions が空です");
    }

    // includeExtensions は ConfigLoader 側で正規化済みの想定だが、ここでも安全にSet化する
    Set<String> allowed = new HashSet<>(includeExtensions);

    try (Stream<Path> paths = Files.walk(absInput)) {
      return paths
          .filter(Files::isRegularFile)
          .map(p -> toMediaItem(absInput, p, allowed))
          .filter(Objects::nonNull)
          .sorted(comparator(sort))
          .toList();
    }
  }

  /**
   * 走査対象のファイルパスから {@link MediaItem} を組み立てる。
   *
   * <ul>
   *   <li>拡張子が許可リストに含まれるか判定（対象外は {@code null} を返す）
   *   <li>sourcePath を絶対パスに正規化し、inputDir からの相対パス（relativePath）を算出
   *   <li>拡張子が {@code mp4} の場合は VIDEO、それ以外は IMAGE として種別決定
   *   <li>ファイルサイズと更新日時を取得して {@link MediaItem} に格納
   * </ul>
   *
   * <p>注意：メタ情報の取得に失敗した場合は、原因ファイルが分かるよう {@link RuntimeException} として投げる （呼び出し側でエラー表示する前提）。
   *
   * @param absInput 入力ディレクトリ（絶対パスに正規化済み）
   * @param file 対象ファイル
   * @param allowedExtensions 許可拡張子（例: jpg, png, mp4）
   * @return 対象であれば {@link MediaItem}、対象外であれば {@code null}
   */
  private static MediaItem toMediaItem(Path absInput, Path file, Set<String> allowedExtensions) {
    String matchedExt = MediaFilter.matchExtension(file, allowedExtensions);
    if (matchedExt.isEmpty()) {
      return null;
    }

    Path absFile = file.toAbsolutePath().normalize();
    Path rel = absInput.relativize(absFile);

    MediaType type = matchedExt.equals("mp4") ? MediaType.VIDEO : MediaType.IMAGE;

    try {
      MediaMetadataReader.Metadata meta = MediaMetadataReader.read(file);
      long size = meta.sizeBytes();
      Instant modified = meta.lastModifiedAt();
      return new MediaItem(absFile, rel, type, size, modified);
    } catch (IOException e) {
      throw new RuntimeException("メタ情報の取得に失敗しました: " + file + " (" + e.getMessage() + ")", e);
    }
  }

  /**
   * 走査結果の並び順（ソート）を定義した Comparator を返す。
   *
   * <p>{@link SortMode} に対応し、以下の順で並べる：
   *
   * <ol>
   *   <li>更新日時（lastModifiedAt）の降順（新しいものが先）
   *   <li>同一更新日時の場合、relativePath の昇順（大小無視）
   * </ol>
   *
   * <p>relativePath の比較は OS 差を避けるため、区切り文字を {@code '/'} に寄せた文字列で比較する。
   *
   * @param sort ソートモード
   * @return ソート用 Comparator
   */
  private static Comparator<MediaItem> comparator(SortMode sort) {
    // modified_desc のみ
    Comparator<MediaItem> byModifiedDesc =
        Comparator.comparing(MediaItem::lastModifiedAt).reversed();

    Comparator<MediaItem> byPathAsc =
        Comparator.comparing(
            item -> item.relativePath().toString().replace('\\', '/'),
            String.CASE_INSENSITIVE_ORDER);

    return byModifiedDesc.thenComparing(byPathAsc);
  }
}
