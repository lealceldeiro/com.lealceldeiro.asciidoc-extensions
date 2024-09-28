package com.lealceldeiro.asciidoc.extensions.calc;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLogger;
import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLoggerFactory;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class CalcMacroTest {
  private Calc<String, String, Map<String, Object>> calcMacro;

  @BeforeEach
  void setUp() {
    calcMacro = new CalcMacro();
  }

  static Stream<Arguments> processSumSrc() {
    return Stream.of(arguments(Map.of("0", "2", "1", "2"), "4.00"),
                     arguments(Map.of("mode", "ignore_invalid", "0", "2", "1", "2"), "4.00"));
  }

  @ParameterizedTest
  @MethodSource("processSumSrc")
  void processSum(Map<String, Object> attributes, String expected) {
    // given
    String operation = CalcMacro.SUM;

    try (MockedStatic<ExtensionLoggerFactory> elf = Mockito.mockStatic(ExtensionLoggerFactory.class)) {
      ExtensionLogger loggerMock = Mockito.mock(ExtensionLogger.class);
      elf.when(ExtensionLoggerFactory::getInstance).thenReturn(loggerMock);

      // when
      String result = calcMacro.calculate(operation, attributes);

      // then
      Assertions.assertEquals(expected, result);
    }
  }
}
