package com.lealceldeiro.asciidoc.extensions.calc;

import com.lealceldeiro.asciidoc.extensions.InvalidValue;
import com.lealceldeiro.asciidoc.extensions.Operator;
import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLogger;
import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLoggerFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
  public String calculate(String operator, Map<String, Object> attributes) {
    logger.log(this, "Operator: " + operator);
    logger.log(this, "Attributes: " + attributes);

    boolean ignoreInvalid = ignoreInvalid(attributes);
    if (ignoreInvalid) {
      logger.log(this, "Ignoring invalid attributes");
    }

    List<BigDecimal> numbers = getNumbers(attributes);
    int expectedNumbersCount = attributes.size() - positionalAttributesCount(attributes);
    if (!ignoreInvalid && numbers.size() != expectedNumbersCount) {
      return InvalidValue.NOT_A_NUMBER;
    }

    Optional<BigDecimal> value = Optional.empty();
    switch (operator) {
      case Operator.SUM:
        value = calc(numbers, BigDecimal::add);
        break;
      case Operator.SUB:
        value = calc(numbers, BigDecimal::subtract);
        break;
      case Operator.MULTIPLY:
        value = calc(numbers, BigDecimal::multiply);
        break;
      case Operator.DIVIDE:
        value = calc(numbers, BigDecimal::divide);
        break;
      default:
        return InvalidValue.NOT_AN_OPERATION;
    }

    return value.map(val -> val.setScale(2, RoundingMode.CEILING))
                .map(BigDecimal::toString)
                .orElse(InvalidValue.NOT_A_VALID_MATH);
  }

  private static int positionalAttributesCount(Map<String, Object> attributes) {
    return attributes.containsKey(MODE) ? 1 : 0;
  }

  private static boolean ignoreInvalid(Map<String, Object> attributes) {
    return attributes.containsKey(MODE)
           && IGNORE_INVALID.equals(String.valueOf(attributes.get(MODE)));
  }

  private List<BigDecimal> getNumbers(Map<String, Object> attributes) {
    return attributes.entrySet()
                     .stream()
                     .filter(entry -> !MODE.equals(entry.getKey()))
                     .filter(entry -> isIntValue(entry.getKey()))
                     .sorted((entry1, entry2) -> {
                       int key1 = Integer.parseInt(entry1.getKey());
                       int key2 = Integer.parseInt(entry2.getKey());
                       return key1 - key2;
                     })
                     .map(Map.Entry::getValue)
                     .map(this::getBigDecimal)
                     .filter(Optional::isPresent)
                     .map(Optional::get)
                     .collect(Collectors.toList());
  }

  private static boolean isIntValue(String rawValue) {
    try {
      Integer.parseInt(rawValue);
    } catch (NullPointerException | NumberFormatException e) {
      return false;
    }
    return true;
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

  private Optional<BigDecimal> calc(Collection<BigDecimal> numbers,
                                    BinaryOperator<BigDecimal> operation) {
    BigDecimal result = null;
    for (BigDecimal number : numbers) {
      if (result == null) {
        result = number;
      } else {
        try {
          result = operation.apply(result, number);
        } catch (ArithmeticException e) {
          return Optional.empty();
        }
      }
    }
    return Optional.ofNullable(result);
  }
}
