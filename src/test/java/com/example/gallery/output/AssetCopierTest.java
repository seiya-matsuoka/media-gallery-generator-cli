package com.example.gallery.output;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.gallery.domain.MediaItem;
import com.example.gallery.domain.MediaType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AssetCopierTest {

  @TempDir Path tempDir;

  // relativePath の相対構造を維持したまま、assets配下にコピーされることを確認する
  @Test
  void copiesFilesPreservingRelativeStructure() throws Exception {
    Path input = tempDir.resolve("media");
    Path assets = tempDir.resolve("dist/assets");
    Files.createDirectories(input.resolve("nested"));

    Path src1 = input.resolve("a.jpg");
    Path src2 = input.resolve("nested/b.png");
    Files.writeString(src1, "A");
    Files.writeString(src2, "B");

    List<MediaItem> items =
        List.of(
            new MediaItem(src1, Path.of("a.jpg"), MediaType.IMAGE, 1L, Instant.now()),
            new MediaItem(src2, Path.of("nested/b.png"), MediaType.IMAGE, 1L, Instant.now()));

    int copied = AssetCopier.copyAll(items, assets);
    assertEquals(2, copied);

    Path dst1 = assets.resolve("a.jpg");
    Path dst2 = assets.resolve("nested/b.png");
    assertTrue(Files.exists(dst1));
    assertTrue(Files.exists(dst2));
    assertEquals("A", Files.readString(dst1));
    assertEquals("B", Files.readString(dst2));
  }

  // relativePath に .. が含まれる場合は拒否されることを確認する
  @Test
  void rejectsPathTraversal() throws Exception {
    Path assets = tempDir.resolve("dist/assets");
    Files.createDirectories(assets);

    Path src = tempDir.resolve("media/a.jpg");
    Files.createDirectories(src.getParent());
    Files.writeString(src, "A");

    List<MediaItem> items =
        List.of(new MediaItem(src, Path.of("../evil.txt"), MediaType.IMAGE, 1L, Instant.now()));

    assertThrows(AssetCopyException.class, () -> AssetCopier.copyAll(items, assets));
  }

  // relativePath が絶対パスの場合は拒否されることを確認する
  @Test
  void rejectsAbsoluteRelativePath() throws Exception {
    Path assets = tempDir.resolve("dist/assets");
    Files.createDirectories(assets);

    Path src = tempDir.resolve("media/a.jpg");
    Files.createDirectories(src.getParent());
    Files.writeString(src, "A");

    Path absoluteRel = tempDir.resolve("abs.txt"); // これは絶対パス
    List<MediaItem> items =
        List.of(new MediaItem(src, absoluteRel, MediaType.IMAGE, 1L, Instant.now()));

    assertThrows(AssetCopyException.class, () -> AssetCopier.copyAll(items, assets));
  }
}
