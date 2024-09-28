package com.lealceldeiro.asciidoc.extensions.calclogger;

public final class ExtensionLoggerFactory {
  private static final ExtensionLogger INSTANCE = new AsciidoctorLogger();

  private ExtensionLoggerFactory() {
    // noop
  }

  public static ExtensionLogger getInstance() {
    return INSTANCE;
  }
}
