package com.lealceldeiro.asciidoc.extensions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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
@Name("calc_date")
@PositionalAttributes({"date", "value", "format", "mode"})
public class CalcDateMacro extends InlineMacroProcessor {
  public static final String NOT_A_NUMBER = "NaN";
  public static final String NOT_A_DATE = "NaD";
  public static final String NOT_A_FORMAT = "NaF";
  public static final String SUM = "sum";
  public static final String SUB = "sub";

  public static final String MODE = "mode";
  public static final String IGNORE_INVALID = "ignore_invalid";

  public static final String DATE_KEY = "date";
  public static final String VALUE_KEY = "value";
  public static final String FORMAT_KEY = "format";

  @Override
  public Object process(ContentNode parent, String target, Map<String, Object> attributes) {
    String calcResult = calculate(target, attributes);

    // https://docs.asciidoctor.org/pdf-converter/latest/extend/create-converter/#override-a-method
    // https://www.rubydoc.info/gems/asciidoctor-pdf/Asciidoctor/PDF/Converter#convert_inline_quoted-instance_method
    return createPhraseNode(parent, "quoted", calcResult, Collections.emptyMap());
  }

  private String calculate(String operation, Map<String, Object> attributes) {
    logDebug("Operation on optionalDate: " + operation);
    logDebug("Attributes: " + attributes);

    boolean ignoreInvalid = ignoreInvalid(attributes);
    if (ignoreInvalid) {
      logDebug("Ignoring invalid attributes");
    }

    Optional<LocalDate> optionalDate = getDate(attributes, ignoreInvalid);
    Optional<Long> optionalAmount = getAmount(attributes, ignoreInvalid);
    Optional<DateTimeFormatter> optionalFormatter = getFormat(attributes, ignoreInvalid);
    TemporalUnit unit = getTemporalUnit(attributes);

    if (optionalDate.isEmpty()) {
      return NOT_A_DATE;
    }
    if (optionalAmount.isEmpty()) {
      return NOT_A_NUMBER;
    }
    if (optionalFormatter.isEmpty()) {
      return NOT_A_FORMAT;
    }

    LocalDate date = optionalDate.get();
    long amount = optionalAmount.get();
    DateTimeFormatter formatter = optionalFormatter.get();
    logDebug("Performing " + operation + " on date: " + date + " with amount: " + amount + " and formatter: " + formatter);

    switch (operation) {
      case SUM:
        return formatter.format(date.plus(amount, unit));
      case SUB:
        return formatter.format(date.minus(amount, unit));
    }

    return NOT_A_DATE;
  }

  private void logDebug(String message) {
    log(new LogRecord(Severity.DEBUG, message));
  }

  private static boolean ignoreInvalid(Map<String, Object> attributes) {
    return attributes.containsKey(MODE)
           && IGNORE_INVALID.equals(String.valueOf(attributes.get(MODE)));
  }

  private Optional<LocalDate> getDate(Map<String, Object> attributes, boolean ignoreInvalid) {
    Object dateValue = attributes.containsKey(DATE_KEY)
                       ? attributes.get(DATE_KEY)
                       : attributes.get("1");
    LocalDate date = null;
    try {
      date = LocalDate.parse(dateValue.toString());
    } catch (NullPointerException | DateTimeParseException e) {
      if (ignoreInvalid) {
        date = LocalDate.now();
      }
    }
    return Optional.ofNullable(date);
  }

  private Optional<Long> getAmount(Map<String, Object> attr, boolean ignoreInvalid) {
    Object value = attr.containsKey(VALUE_KEY) ? attr.get(VALUE_KEY) : attr.get("2");
    String valueString = String.valueOf(value);
    if (valueString.endsWith("d") || valueString.endsWith("m") || valueString.endsWith("y")) {
      String rawValue = valueString.substring(0, valueString.length() - 1);

      return getLong(rawValue, ignoreInvalid);
    }
    return getLong(valueString, ignoreInvalid);
  }

  private Optional<DateTimeFormatter> getFormat(Map<String, Object> attr, boolean ignoreInvalid) {
    String value = String.valueOf(attr.containsKey(FORMAT_KEY)
                                  ? attr.get(FORMAT_KEY)
                                  : attr.get("3"));
    logDebug("Raw format: " + value);

    DateTimeFormatter dateTimeFormatter = null;
    try {
      dateTimeFormatter = DateTimeFormatter.ofPattern(value, Locale.ENGLISH);
    } catch (NullPointerException | IllegalArgumentException | DateTimeParseException e) {
      if (ignoreInvalid) {
        dateTimeFormatter = DateTimeFormatter.ISO_DATE;
      }
    }
    return Optional.ofNullable(dateTimeFormatter);
  }

  private static Optional<Long> getLong(String rawValue, boolean ignoreInvalid) {
    Long val = null;
    try {
      val = Long.parseLong(rawValue);
    } catch (NullPointerException | NumberFormatException e) {
      if (ignoreInvalid) {
        val = 0L;
      }
    }
    return Optional.ofNullable(val);
  }

  private TemporalUnit getTemporalUnit(Map<String, Object> attributes) {
    Object value = attributes.containsKey(VALUE_KEY)
                   ? attributes.get(VALUE_KEY)
                   : attributes.get("2");
    String valueString = String.valueOf(value);
    String rawValue = "d";
    if (valueString.endsWith("d") || valueString.endsWith("m") || valueString.endsWith("y")) {
      rawValue = valueString.substring(valueString.length() - 1);
    }
    switch (rawValue) {
      case "y":
        return ChronoUnit.YEARS;
      case "m":
        return ChronoUnit.MONTHS;
      case "d":
      default:
        return ChronoUnit.DAYS;
    }
  }
}
