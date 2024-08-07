package com.lealceldeiro.asciidoc.extensions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Name;
import org.asciidoctor.extension.PositionalAttributes;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;

/**
 * Docs at
 * <a href="https://docs.asciidoctor.org/asciidoctorj/latest/extensions/extensions-introduction/">
 * AsciidocJ Extensions API
 * </a>
 */
@Name("calc")
@PositionalAttributes("mode")
public class CalcMacro extends InlineMacroProcessor {
  public static final String NOT_A_NUMBER = "NaN";
  public static final String NOT_AN_OPERATION = "NaO";
  public static final String SUM = "sum";
  public static final String SUB = "sub";
  public static final String MULTIPLY = "multiply";
  public static final String DIVIDE = "divide";

  public static final String MODE = "mode";
  public static final String IGNORE_INVALID = "ignore_invalid";
  public static final int NUMBER_OF_POSITION_ATTRIBUTES = 1;

  @Override
  public Object process(ContentNode parent, String target, Map<String, Object> attributes) {
    String calcResult = calculate(target, attributes);

    // https://docs.asciidoctor.org/pdf-converter/latest/extend/create-converter/#override-a-method
    // https://www.rubydoc.info/gems/asciidoctor-pdf/Asciidoctor/PDF/Converter#convert_inline_quoted-instance_method
    return createPhraseNode(parent, "quoted", calcResult, Collections.emptyMap());
  }

  private String calculate(String operation, Map<String, Object> attributes) {
    logDebug("Operation: " + operation);
    logDebug("Attributes: " + attributes);

    boolean ignoreInvalid = ignoreInvalid(attributes);
    if (ignoreInvalid) {
      logDebug("Ignoring invalid attributes");
    }

    Collection<BigDecimal> numbers = getNumbers(attributes);
    if (!ignoreInvalid && (numbers.size() != (attributes.size() - NUMBER_OF_POSITION_ATTRIBUTES))) {
      return NOT_A_NUMBER;
    }

    BigDecimal value = null;
    switch (operation) {
      case SUM:
        value = calc(numbers, BigDecimal::add);
        break;
      case SUB:
        value = calc(numbers, BigDecimal::subtract);
        break;
      case MULTIPLY:
        value = calc(numbers, BigDecimal::multiply);
        break;
      case DIVIDE:
        value = calc(numbers, BigDecimal::divide);
        break;
    }

    return value != null
           ? value.setScale(2, RoundingMode.CEILING).toString()
           : NOT_AN_OPERATION;
  }

  private static boolean ignoreInvalid(Map<String, Object> attributes) {
    return attributes.containsKey(MODE)
           && IGNORE_INVALID.equals(String.valueOf(attributes.get(MODE)));
  }

  private Collection<BigDecimal> getNumbers(Map<String, Object> attributes) {
    return attributes.entrySet()
                     .stream()
                     .filter(entry -> !MODE.equals(entry.getKey()))
                     .map(Map.Entry::getValue)
                     .map(this::getBigDecimal)
                     .filter(Optional::isPresent)
                     .map(Optional::get)
                     .collect(Collectors.toList());
  }

  private void logDebug(String message) {
    log(new LogRecord(Severity.DEBUG, message));
  }

  private Optional<BigDecimal> getBigDecimal(Object value) {
    if (value == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(new BigDecimal(String.valueOf(value)));
    } catch (NullPointerException | NumberFormatException e) {
      return Optional.empty();
    }
  }

  private BigDecimal calc(Collection<BigDecimal> numbers, BinaryOperator<BigDecimal> operation) {
    return numbers.stream().reduce(operation).orElse(BigDecimal.ZERO);
  }
}
