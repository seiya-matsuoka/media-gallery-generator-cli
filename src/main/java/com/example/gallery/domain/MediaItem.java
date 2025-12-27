package com.example.gallery.domain;

import java.nio.file.Path;
import java.time.Instant;

/**
 * 入力フォルダ内で見つかったメディアファイル1件分の情報。
 *
 * <p>relativePath は inputDir からの相対パス（コピー/HTML生成で使用）。
 */
public record MediaItem(
    Path sourcePath, Path relativePath, MediaType type, long sizeBytes, Instant lastModifiedAt) {}
