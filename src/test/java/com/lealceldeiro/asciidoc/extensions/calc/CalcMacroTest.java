package com.lealceldeiro.asciidoc.extensions.calc;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.lealceldeiro.asciidoc.extensions.InvalidValue;
import com.lealceldeiro.asciidoc.extensions.Operator;
import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLogger;
import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLoggerFactory;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class CalcMacroTest {
  static Stream<Arguments> calculateSrc() {
    return Stream.of(
        arguments(Operator.SUM, Map.of("0", "2", "1", "2"), "4.00"),
        arguments(Operator.SUM, Map.of("0", "not a number", "1", "2"), InvalidValue.NOT_A_NUMBER),
        arguments(Operator.SUM, Map.of("mode", "ignore_invalid", "0", "2", "1", "2"), "4.00"),
        arguments(Operator.SUM, Map.of("mode", "ignore_invalid", "0", "not a number", "1", "2"), "2.00"),
        arguments(Operator.SUM, Map.of("0", "-4", "1", "5"), "1.00"),
        arguments(Operator.SUM, Map.of("0", "-4", "1", "-5"), "-9.00"),

        arguments("not an operator", Map.of("mode", "ignore_invalid", "0", "not a number", "1", "2"), InvalidValue.NOT_AN_OPERATION),
        arguments("not an operator", Map.of("mode", "ignore_invalid", "0", "2", "1", "2"), InvalidValue.NOT_AN_OPERATION),
        arguments("not an operator", Map.of("0", "2", "1", "2"), InvalidValue.NOT_AN_OPERATION),
        arguments("not an operator", Map.of(), InvalidValue.NOT_AN_OPERATION),

        arguments(Operator.SUB, Map.of("0", "2", "1", "2"), "0.00"),
        arguments(Operator.SUB, Map.of("0", "not a number", "1", "2"), InvalidValue.NOT_A_NUMBER),
        arguments(Operator.SUB, Map.of("mode", "ignore_invalid", "0", "2", "1", "2"), "0.00"),
        arguments(Operator.SUB, Map.of("0", "0", "1", "2"), "-2.00"),
        arguments(Operator.SUB, Map.of("0", "2", "1", "0"), "2.00"),
        arguments(Operator.SUB, Map.of("0", "-4", "1", "-3"), "-1.00"),
        arguments(Operator.SUB, Map.of("0", "-4", "1", "3"), "-7.00"),
        arguments(Operator.SUB, Map.of("mode", "ignore_invalid", "0", "not a number", "1", "2"), "2.00"),

        arguments(Operator.MULTIPLY, Map.of("0", "2", "1", "2"), "4.00"),
        arguments(Operator.MULTIPLY, Map.of("0", "not a number", "1", "2"), InvalidValue.NOT_A_NUMBER),
        arguments(Operator.MULTIPLY, Map.of("mode", "ignore_invalid", "0", "2", "1", "2"), "4.00"),
        arguments(Operator.MULTIPLY, Map.of("0", "0", "1", "2"), "0.00"),
        arguments(Operator.MULTIPLY, Map.of("0", "2", "1", "0"), "0.00"),
        arguments(Operator.MULTIPLY, Map.of("0", "-4", "1", "-3"), "12.00"),
        arguments(Operator.MULTIPLY, Map.of("0", "-4", "1", "3"), "-12.00"),
        arguments(Operator.MULTIPLY, Map.of("mode", "ignore_invalid", "0", "not a number", "1", "2"), "2.00"),

        arguments(Operator.DIVIDE, Map.of("0", "2", "1", "2"), "1.00"),
        arguments(Operator.DIVIDE, Map.of("0", "not a number", "1", "2"), InvalidValue.NOT_A_NUMBER),
        arguments(Operator.DIVIDE, Map.of("mode", "ignore_invalid", "0", "2", "1", "2"), "1.00"),
        arguments(Operator.DIVIDE, Map.of("0", "0", "1", "2"), "0.00"),
        arguments(Operator.DIVIDE, Map.of("0", "2", "1", "0"), InvalidValue.NOT_A_VALID_MATH),
        arguments(Operator.DIVIDE, Map.of("0", "-4", "1", "-1"), "4.00"),
        arguments(Operator.DIVIDE, Map.of("0", "-4", "1", "1"), "-4.00"),
        arguments(Operator.DIVIDE, Map.of("0", "4", "1", "-1"), "-4.00"),
        arguments(Operator.DIVIDE, Map.of("mode", "ignore_invalid", "0", "not a number", "1", "2"), "2.00")
                    );
  }

  @ParameterizedTest
  @MethodSource("calculateSrc")
  void calculate(String operation, Map<String, Object> attributes, String expected) {
    try (MockedStatic<ExtensionLoggerFactory> elf = Mockito.mockStatic(ExtensionLoggerFactory.class)) {
      ExtensionLogger loggerMock = Mockito.mock(ExtensionLogger.class);
      elf.when(ExtensionLoggerFactory::getInstance).thenReturn(loggerMock);

      Calc<String, String, Map<String, Object>> calcMacro = new CalcMacro();
      String result = calcMacro.calculate(operation, attributes);

      Assertions.assertEquals(expected, result);
    }
  }
}
