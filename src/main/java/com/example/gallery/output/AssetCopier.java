package com.example.gallery.output;

import com.example.gallery.domain.MediaItem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

/** メディアファイルを dist/assets 配下へコピーする（相対構造維持）。 */
public final class AssetCopier {

  private AssetCopier() {}

  /**
   * メディア一覧を assetsDir 配下へコピーする。
   *
   * <p>コピー先は {@code assetsDir/relativePath} （ディレクトリは必要に応じて作成）。 安全のため relativePath が絶対パス、 または {@code
   * ..} を含む場合は拒否する。
   *
   * @param items コピー対象のメディア一覧
   * @param assetsDir dist/assets のパス
   * @return コピーした件数
   * @throws IOException コピーに失敗した場合
   * @throws AssetCopyException relativePath が危険/不正な場合
   */
  public static int copyAll(List<MediaItem> items, Path assetsDir) throws IOException {
    Objects.requireNonNull(items, "items");
    Objects.requireNonNull(assetsDir, "assetsDir");

    Path absAssetsDir = assetsDir.toAbsolutePath().normalize();
    Files.createDirectories(absAssetsDir);

    int count = 0;
    for (MediaItem item : items) {
      Path target = resolveTargetPath(absAssetsDir, item.relativePath());
      Files.createDirectories(target.getParent());

      // 既に存在していたら上書き
      Files.copy(item.sourcePath(), target, StandardCopyOption.REPLACE_EXISTING);
      count++;
    }
    return count;
  }

  private static Path resolveTargetPath(Path absAssetsDir, Path relativePath) {
    if (relativePath == null) {
      throw new AssetCopyException("relativePath が null です");
    }
    if (relativePath.isAbsolute()) {
      throw new AssetCopyException("relativePath が絶対パスです: " + relativePath);
    }

    Path normalizedRel = relativePath.normalize();
    for (Path part : normalizedRel) {
      if ("..".equals(part.toString())) {
        throw new AssetCopyException("relativePath に .. が含まれています: " + relativePath);
      }
    }

    Path target = absAssetsDir.resolve(normalizedRel).normalize();
    if (!target.startsWith(absAssetsDir)) {
      throw new AssetCopyException(
          "コピー先が assetsDir 配下ではありません: target=" + target + ", assetsDir=" + absAssetsDir);
    }
    return target;
  }
}
