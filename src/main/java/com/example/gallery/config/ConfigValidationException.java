package com.example.gallery.config;

/** 設定ファイルの内容が不正な場合の例外。 */
public class ConfigValidationException extends RuntimeException {
  public ConfigValidationException(String message) {
    super(message);
  }
}
