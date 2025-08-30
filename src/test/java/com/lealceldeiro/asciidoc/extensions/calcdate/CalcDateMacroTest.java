package com.lealceldeiro.asciidoc.extensions.calcdate;

import static com.lealceldeiro.asciidoc.extensions.calcdate.CalcDateMacro.AMOUNT_ATTRIBUTE_POSITION;
import static com.lealceldeiro.asciidoc.extensions.calcdate.CalcDateMacro.DATE_ATTRIBUTE_POSITION;
import static com.lealceldeiro.asciidoc.extensions.calcdate.CalcDateMacro.FROM_ZONE_ID_ATTRIBUTE_POSITION;
import static com.lealceldeiro.asciidoc.extensions.calcdate.CalcDateMacro.TO_ZONE_ID_ATTRIBUTE_POSITION;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.lealceldeiro.asciidoc.extensions.Calc;
import com.lealceldeiro.asciidoc.extensions.InvalidValue;
import com.lealceldeiro.asciidoc.extensions.Macro;
import com.lealceldeiro.asciidoc.extensions.Operator;
import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLogger;
import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLoggerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class CalcDateMacroTest {
  private static final String NO_ZONE_ID = "-";

  static Stream<Arguments> calculateSrc() {
    return Stream.of(
        arguments(Operator.SUM,
                  Map.of(DATE_ATTRIBUTE_POSITION, "2024-01-01",
                         AMOUNT_ATTRIBUTE_POSITION, "2d"),
                  "2024-01-03"),
        arguments(Operator.SUM,
                  Map.of(DATE_ATTRIBUTE_POSITION, "2024-01-01",
                         AMOUNT_ATTRIBUTE_POSITION, "5"),
                  "2024-01-06"),
        arguments(Operator.SUM,
                  Map.of(DATE_ATTRIBUTE_POSITION, "2024-01-01",
                         AMOUNT_ATTRIBUTE_POSITION, "1m"),
                  "2024-02-01"),
        arguments(Operator.SUM,
                  Map.of(DATE_ATTRIBUTE_POSITION, "2024-01-01",
                         AMOUNT_ATTRIBUTE_POSITION, "1y"),
                  "2025-01-01"),
        arguments(Operator.SUM,
                  Map.of(Macro.Key.DATE, "2024-01-01",
                         Macro.Key.VALUE, "2d",
                         Macro.Key.TARGET_FORMAT, "d MMM yy"),
                  "3 Jan 24"),
        arguments(Operator.SUM,
                  Map.of(Macro.Key.DATE, "2024-01-01",
                         Macro.Key.VALUE, "0",
                         Macro.Key.TARGET_FORMAT, "d MMM yy"),
                  "1 Jan 24"),
        arguments(Operator.SUM,
                  Map.of(DATE_ATTRIBUTE_POSITION, "2024-01-01",
                         Macro.Key.VALUE, "0",
                         Macro.Key.TARGET_FORMAT, "d MMM yy"),
                  "1 Jan 24"),
        arguments(Operator.SUM,
                  Map.of(DATE_ATTRIBUTE_POSITION, "2024-01-01",
                         AMOUNT_ATTRIBUTE_POSITION, "1y",
                         Macro.Key.TARGET_FORMAT, "MMM d, yyyy"),
                  "Jan 1, 2025"),

        arguments("not a valid operation",
                  Map.of(DATE_ATTRIBUTE_POSITION, "2024-01-01",
                         AMOUNT_ATTRIBUTE_POSITION, "2d"),
                  InvalidValue.NOT_AN_OPERATION),
        arguments(Operator.MULTIPLY,
                  Map.of(DATE_ATTRIBUTE_POSITION, "2024-01-01",
                         AMOUNT_ATTRIBUTE_POSITION, "1"),
                  InvalidValue.NOT_AN_OPERATION),
        arguments(Operator.DIVIDE,
                  Map.of(DATE_ATTRIBUTE_POSITION, "2024-01-01",
                         AMOUNT_ATTRIBUTE_POSITION, "1"),
                  InvalidValue.NOT_AN_OPERATION),

        arguments(Operator.SUM,
                  Map.of(DATE_ATTRIBUTE_POSITION, "not a date",
                         AMOUNT_ATTRIBUTE_POSITION, "1"),
                  InvalidValue.NOT_A_DATE),
        arguments(Operator.SUM,
                  Map.of(DATE_ATTRIBUTE_POSITION, "1st of Jan 2024",
                         AMOUNT_ATTRIBUTE_POSITION, "1"),
                  InvalidValue.NOT_A_DATE),
        arguments(Operator.SUM,
                  Map.of(DATE_ATTRIBUTE_POSITION, "2024/01/01",
                         AMOUNT_ATTRIBUTE_POSITION, "1"),
                  InvalidValue.NOT_A_DATE),

        arguments(Operator.SUM,
                  Map.of(DATE_ATTRIBUTE_POSITION, "2024-01-01",
                         AMOUNT_ATTRIBUTE_POSITION, "1",
                         CalcDateMacro.TARGET_FORMAT_ATTRIBUTE_POSITION,
                         "not a valid output date format"),
                  InvalidValue.NOT_A_FORMAT),

        arguments(Operator.SUM,
                  Map.of(DATE_ATTRIBUTE_POSITION, "2024-01-01",
                         AMOUNT_ATTRIBUTE_POSITION, "not a valid number"),
                  InvalidValue.NOT_A_NUMBER),
        arguments(Operator.SUM,
                  Map.of(DATE_ATTRIBUTE_POSITION, "2024-01-01",
                         AMOUNT_ATTRIBUTE_POSITION, "oney"),
                  InvalidValue.NOT_A_NUMBER),
        arguments(Operator.SUM,
                  Map.of(DATE_ATTRIBUTE_POSITION, "2024-01-01",
                         AMOUNT_ATTRIBUTE_POSITION, "1w"),
                  InvalidValue.NOT_A_NUMBER),

        arguments(Operator.SUM,
                  Map.of(Macro.Key.MODE, Macro.Value.IGNORE_INVALID,
                         DATE_ATTRIBUTE_POSITION, "2024-01-01",
                         AMOUNT_ATTRIBUTE_POSITION, "x"),
                  "2024-01-01"),
        arguments(Operator.SUM,
                  Map.of(CalcDateMacro.MODE_ATTRIBUTE_POSITION, Macro.Value.IGNORE_INVALID,
                         DATE_ATTRIBUTE_POSITION, "not a date",
                         AMOUNT_ATTRIBUTE_POSITION, "1d"),
                  LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE)),
        arguments(Operator.SUM,
                  Map.of(Macro.Key.MODE, Macro.Value.IGNORE_INVALID,
                         DATE_ATTRIBUTE_POSITION, "2024-01-01",
                         AMOUNT_ATTRIBUTE_POSITION, "1d",
                         Macro.Key.TARGET_FORMAT, "not a valid format"),
                  "2024-01-02"),

        arguments(Operator.SUB,
                  Map.of(DATE_ATTRIBUTE_POSITION, "2024-01-01",
                         AMOUNT_ATTRIBUTE_POSITION, "2d"),
                  "2023-12-30"),
        arguments(Operator.SUB,
                  Map.of(DATE_ATTRIBUTE_POSITION, "2024-01-01",
                         AMOUNT_ATTRIBUTE_POSITION, "5"),
                  "2023-12-27"),
        arguments(Operator.SUB,
                  Map.of(DATE_ATTRIBUTE_POSITION, "2024-01-01",
                         AMOUNT_ATTRIBUTE_POSITION, "1m"),
                  "2023-12-01"),
        arguments(Operator.SUB,
                  Map.of(DATE_ATTRIBUTE_POSITION, "2024-01-01",
                         AMOUNT_ATTRIBUTE_POSITION, "1y"),
                  "2023-01-01"),
        arguments(Operator.SUM,
                  Map.of(Macro.Key.DATE, "2024-01-01",
                         Macro.Key.VALUE, "1y",
                         Macro.Key.FROM_ZONE_ID, "invalid zone id"),
                  "2025-01-01"),
        arguments(Operator.SUM,
                  Map.of(Macro.Key.DATE, "2024-01-01",
                         Macro.Key.VALUE, "1y",
                         Macro.Key.TO_ZONE_ID, "invalid zone id"),
                  "2025-01-01"),
        arguments(Operator.SUM,
                  Map.of(Macro.Key.DATE, "2024-01-01",
                         Macro.Key.VALUE, "1y",
                         Macro.Key.FROM_ZONE_ID, "invalid zone id",
                         Macro.Key.TO_ZONE_ID, "also invalid zone id"),
                  "2025-01-01")
                    );
  }

  @ParameterizedTest
  @MethodSource("calculateSrc")
  void calculate(String operation, Map<String, Object> attributes, String expected) {
    try (MockedStatic<ExtensionLoggerFactory> elf = Mockito.mockStatic(ExtensionLoggerFactory.class)) {
      ExtensionLogger loggerMock = Mockito.mock(ExtensionLogger.class);
      elf.when(ExtensionLoggerFactory::getInstance).thenReturn(loggerMock);

      Calc<Map<String, Object>> calcDateMacro = new CalcDateMacro();
      String result = calcDateMacro.calculate(operation, attributes);

      Assertions.assertEquals(expected, result);
    }
  }

  private static Stream<Arguments> calculateDateWithZoneIdsSrc() {
    Set<String> zoneIds = Set.of(
        "UTC", "America/Adak", "America/Anchorage", "America/Los_Angeles", "America/Chicago",
        "America/New_York", "America/Phoenix", "America/Denver", "Asia/Yangon",
        "Australia/Broken_Hill", "Europe/Athens", "Europe/Madrid", "Pacific/Tarawa", NO_ZONE_ID
                                );
    return zoneIds.stream()
                  .flatMap(fromZoneId -> zoneIds.stream()
                                                .map(toZoneId -> arguments(fromZoneId, toZoneId)));
  }

  @ParameterizedTest
  @MethodSource("calculateDateWithZoneIdsSrc")
  void calculateWithTimeZoneSet(String fromZoneIdArg, String toZoneIdArg) {
    try (MockedStatic<ExtensionLoggerFactory> elf = Mockito.mockStatic(ExtensionLoggerFactory.class)) {
      ExtensionLogger loggerMock = Mockito.mock(ExtensionLogger.class);
      elf.when(ExtensionLoggerFactory::getInstance).thenReturn(loggerMock);

      Calc<Map<String, Object>> calcDateMacro = new CalcDateMacro();
      String date = "2025-08-25";
      String fromZoneId = !fromZoneIdArg.equals(NO_ZONE_ID) ? fromZoneIdArg : null;
      String toZoneId = !toZoneIdArg.equals(NO_ZONE_ID) ? toZoneIdArg : null;
      Map<String, Object> attributes = new HashMap<>();
      attributes.put(DATE_ATTRIBUTE_POSITION, date);
      attributes.put(AMOUNT_ATTRIBUTE_POSITION, "0d");
      attributes.put(FROM_ZONE_ID_ATTRIBUTE_POSITION, fromZoneId);
      attributes.put(TO_ZONE_ID_ATTRIBUTE_POSITION, toZoneId);

      String result = calcDateMacro.calculate(Operator.SUM, attributes);

      ZoneId fromZone = zoneIdOrSystemDefault(fromZoneId);
      ZoneId toZone = zoneIdOrSystemDefault(toZoneId);
      LocalDateTime nowAtSrc = LocalDateTime.now(fromZone);
      LocalDate expected = ZonedDateTime.of(LocalDate.parse(date), nowAtSrc.toLocalTime(), fromZone)
                                        .withZoneSameInstant(toZone)
                                        .toLocalDate();
      Assertions.assertEquals(expected.toString(), result);
    }
  }

  private static ZoneId zoneIdOrSystemDefault(String id) {
    if (id == null || NO_ZONE_ID.equals(id)) {
      return ZoneId.systemDefault();
    }
    return ZoneId.of(id);
  }
}
