package com.lealceldeiro.asciidoc.extensions;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Name;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Docs at
 * <a href="https://docs.asciidoctor.org/asciidoctorj/latest/extensions/extensions-introduction/">
 * AsciidocJ Extensions API
 * </a>
 */
@Name("calc")
public class CalcMacro extends InlineMacroProcessor {
  private static final Logger LOGGER = Logger.getLogger(CalcMacro.class.getName());

  @Override
  public Object process(ContentNode parent, String target, Map<String, Object> attributes) {
    String calcResult = calculate(target);

    Map<String, Object> options = new HashMap<>();

    return createPhraseNode(parent, "text", calcResult, options);
  }

  private String calculate(String operation) {
    LOGGER.info("OPERATION: " + operation);
    return "operation is " + operation;
  }
}
