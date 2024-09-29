package com.lealceldeiro.asciidoc.extensions.calcdate;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.lealceldeiro.asciidoc.extensions.Calc;
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

class CalcDateMacroTest {
  static Stream<Arguments> calculateSrc() {
    return Stream.of(
        arguments(Operator.SUM,
                  Map.of("1", "2024-01-01", "2", "2d"),
                  "2024-01-03"),
        arguments(Operator.SUM,
                  Map.of("1", "2024-01-01", "2", "5"),
                  "2024-01-06"),
        arguments(Operator.SUM,
                  Map.of("1", "2024-01-01", "2", "1m"),
                  "2024-02-01"),
        arguments(Operator.SUM,
                  Map.of("1", "2024-01-01", "2", "1y"),
                  "2025-01-01"),
        arguments(Operator.SUM,
                  Map.of("date", "2024-01-01", "value", "2d", "format", "d MMM yy"),
                  "3 Jan 24"),
        arguments(Operator.SUM,
                  Map.of("date", "2024-01-01", "value", "0", "format", "d MMM yy"),
                  "1 Jan 24"),
        arguments(Operator.SUM,
                  Map.of("1", "2024-01-01", "value", "0", "format", "d MMM yy"),
                  "1 Jan 24"),
        arguments(Operator.SUM,
                  Map.of("1", "2024-01-01", "2", "1y", "format", "MMM d, yyyy"),
                  "Jan 1, 2025"),

        arguments("not a valid operation",
                  Map.of("1", "2024-01-01", "2", "2d"),
                  InvalidValue.NOT_AN_OPERATION),
        arguments(Operator.MULTIPLY,
                  Map.of("1", "2024-01-01", "2", "1"),
                  InvalidValue.NOT_AN_OPERATION),
        arguments(Operator.DIVIDE,
                  Map.of("1", "2024-01-01", "2", "1"),
                  InvalidValue.NOT_AN_OPERATION),

        arguments(Operator.SUM,
                  Map.of("1", "not a date", "2", "1"),
                  InvalidValue.NOT_A_DATE),
        arguments(Operator.SUM,
                  Map.of("1", "1st of Jan 2024", "2", "1"),
                  InvalidValue.NOT_A_DATE),
        arguments(Operator.SUM,
                  Map.of("1", "2024/01/01", "2", "1"),
                  InvalidValue.NOT_A_DATE),

        arguments(Operator.SUM,
                  Map.of("1", "2024-01-01", "2", "1", "3", "not a valid output date format"),
                  InvalidValue.NOT_A_FORMAT),

        arguments(Operator.SUM,
                  Map.of("1", "2024-01-01", "2", "not a valid number"),
                  InvalidValue.NOT_A_NUMBER),
        arguments(Operator.SUM,
                  Map.of("1", "2024-01-01", "2", "oney"),
                  InvalidValue.NOT_A_NUMBER),
        arguments(Operator.SUM,
                  Map.of("1", "2024-01-01", "2", "1w"),
                  InvalidValue.NOT_A_NUMBER)
                    );
  }

  @ParameterizedTest
  @MethodSource("calculateSrc")
  void calculate(String operation, Map<String, Object> attributes, String expected) {
    try (MockedStatic<ExtensionLoggerFactory> elf = Mockito.mockStatic(ExtensionLoggerFactory.class)) {
      ExtensionLogger loggerMock = Mockito.mock(ExtensionLogger.class);
      elf.when(ExtensionLoggerFactory::getInstance).thenReturn(loggerMock);

      Calc<String, String, Map<String, Object>> calcDateMacro = new CalcDateMacro();
      String result = calcDateMacro.calculate(operation, attributes);

      Assertions.assertEquals(expected, result);
    }
  }
}
