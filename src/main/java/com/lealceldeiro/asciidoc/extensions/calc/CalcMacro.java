package com.lealceldeiro.asciidoc.extensions.calc;

import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLogger;
import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLoggerFactory;
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

/**
 * Docs at
 * <a href="https://docs.asciidoctor.org/asciidoctorj/latest/extensions/extensions-introduction/">
 * AsciidocJ Extensions API
 * </a>
 */
@Name("calc")
@PositionalAttributes("mode")
public class CalcMacro extends InlineMacroProcessor implements Calc<String, String, Map<String, Object>> {
  private static final ExtensionLogger logger = ExtensionLoggerFactory.getInstance();

  public static final String NOT_A_NUMBER = "NaN";
  public static final String NOT_AN_OPERATION = "NaO";
  public static final String SUM = "sum";
  public static final String SUB = "sub";
  public static final String MULTIPLY = "multiply";
  public static final String DIVIDE = "divide";

  public static final String MODE = "mode";
  public static final String IGNORE_INVALID = "ignore_invalid";

  @Override
  public Object process(ContentNode parent, String target, Map<String, Object> attributes) {
    String calcResult = calculate(target, attributes);

    // https://docs.asciidoctor.org/pdf-converter/latest/extend/create-converter/#override-a-method
    // https://www.rubydoc.info/gems/asciidoctor-pdf/Asciidoctor/PDF/Converter#convert_inline_quoted-instance_method
    return createPhraseNode(parent, "quoted", calcResult, Collections.emptyMap());
  }

  @Override
  public String calculate(String operation, Map<String, Object> attributes) {
    logger.log(this, "Operation: " + operation);
    logger.log(this, "Attributes: " + attributes);

    boolean ignoreInvalid = ignoreInvalid(attributes);
    if (ignoreInvalid) {
      logger.log(this, "Ignoring invalid attributes");
    }

    Collection<BigDecimal> numbers = getNumbers(attributes);
    var expectedNumbersCount = attributes.size() - positionalAttributesCount(attributes);
    if (!ignoreInvalid && numbers.size() != expectedNumbersCount) {
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

  private static int positionalAttributesCount(Map<String, Object> attributes) {
    return attributes.containsKey(MODE) ? 1 : 0;
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
