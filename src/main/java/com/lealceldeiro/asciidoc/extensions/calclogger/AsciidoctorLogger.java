package com.lealceldeiro.asciidoc.extensions.calclogger;

import org.asciidoctor.extension.BaseProcessor;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;

public class AsciidoctorLogger implements ExtensionLogger {
  @Override
  public void log(BaseProcessor processor, String message) {
    try {
      processor.log(new LogRecord(Severity.DEBUG, message));
    } catch (RuntimeException e) {
      System.err.println("Error logging record " + e.getMessage());
    }
  }
}
