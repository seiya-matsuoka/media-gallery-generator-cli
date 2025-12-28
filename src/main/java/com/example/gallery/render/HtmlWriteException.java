package com.example.gallery.render;

/** HTML生成（テンプレ読込/書き込み）に失敗した場合の例外。 */
public class HtmlWriteException extends RuntimeException {
  public HtmlWriteException(String message, Throwable cause) {
    super(message, cause);
  }
}
