package com.example.gallery.config;

import com.example.gallery.domain.SortMode;
import java.nio.file.Path;
import java.util.List;

/**
 * 設定ファイルから読み込んだアプリ設定（解決済み）。
 *
 * <p>input/output は config ファイルの場所を基準に相対解決された {@link Path} を保持する。
 */
public record AppConfig(
    Path inputDir, Path outputDir, List<String> includeExtensions, SortMode sort) {}
