package com.example.gallery.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.gallery.domain.MediaItem;
import com.example.gallery.domain.MediaType;
import com.example.gallery.domain.SortMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MediaScannerTest {

  @TempDir Path tempDir;

  // 対象拡張子（大小無視）だけが再帰走査で収集され、更新日時降順に並ぶことを確認する
  @Test
  void scansRecursivelyFiltersAndSorts() throws Exception {
    Path input = tempDir.resolve("media");
    Files.createDirectories(input.resolve("nested"));

    Path aJpg = input.resolve("a.jpg");
    Path bMp4 = input.resolve("b.MP4");
    Path cTxt = input.resolve("c.txt");
    Path dPng = input.resolve("nested/d.png");

    Files.writeString(aJpg, "a");
    Files.writeString(bMp4, ""); // 空でもOK
    Files.writeString(cTxt, "x");
    Files.writeString(dPng, "d");

    // modified: a(古) → b → d(新)
    Files.setLastModifiedTime(aJpg, FileTime.from(Instant.parse("2020-01-01T00:00:00Z")));
    Files.setLastModifiedTime(bMp4, FileTime.from(Instant.parse("2021-01-01T00:00:00Z")));
    Files.setLastModifiedTime(dPng, FileTime.from(Instant.parse("2022-01-01T00:00:00Z")));

    List<MediaItem> items =
        MediaScanner.scan(input, List.of("jpg", "png", "mp4"), SortMode.MODIFIED_DESC);

    assertEquals(3, items.size());
    assertEquals("nested/d.png", items.get(0).relativePath().toString().replace('\\', '/'));
    assertEquals("b.MP4", items.get(1).relativePath().toString().replace('\\', '/'));
    assertEquals("a.jpg", items.get(2).relativePath().toString().replace('\\', '/'));

    assertEquals(MediaType.IMAGE, items.get(0).type());
    assertEquals(MediaType.VIDEO, items.get(1).type());
  }

  // inputDir が存在しない場合に MediaScanException になることを確認する
  @Test
  void rejectsMissingInputDir() {
    Path missing = tempDir.resolve("missing");
    assertThrows(
        MediaScanException.class,
        () -> MediaScanner.scan(missing, List.of("jpg"), SortMode.MODIFIED_DESC));
  }
}
