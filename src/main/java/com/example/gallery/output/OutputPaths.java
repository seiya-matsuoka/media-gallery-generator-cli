package com.example.gallery.output;

import java.nio.file.Path;

/**
 * 出力先のレイアウト（パス群）。
 *
 * <p>本アプリでは outputDir 配下に index.html と assets/ を作成する。
 */
public record OutputPaths(Path outputDir, Path assetsDir, Path indexHtmlPath) {}
