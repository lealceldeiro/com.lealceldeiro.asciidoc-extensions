package com.lealceldeiro.asciidoc.extensions.calclogger;

import org.asciidoctor.extension.BaseProcessor;

public interface ExtensionLogger {
  void log(BaseProcessor processor, String message);
}
