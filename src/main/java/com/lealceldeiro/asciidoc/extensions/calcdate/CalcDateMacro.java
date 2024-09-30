package com.lealceldeiro.asciidoc.extensions.calcdate;

import com.lealceldeiro.asciidoc.extensions.Calc;
import com.lealceldeiro.asciidoc.extensions.InvalidValue;
import com.lealceldeiro.asciidoc.extensions.Macro;
import com.lealceldeiro.asciidoc.extensions.Operator;
import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLogger;
import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLoggerFactory;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Name;
import org.asciidoctor.extension.PositionalAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Docs at
 * <a href="https://docs.asciidoctor.org/asciidoctorj/latest/extensions/extensions-introduction/">
 * AsciidocJ Extensions API
 * </a>
 */
@Name("calc_date")
@PositionalAttributes({Macro.Key.DATE, Macro.Key.VALUE, Macro.Key.TARGET_FORMAT, Macro.Key.MODE})
public class CalcDateMacro extends InlineMacroProcessor implements Calc<String, String, Map<String, Object>> {
  private static final ExtensionLogger logger = ExtensionLoggerFactory.getInstance();

  static final String DAY = "d";
  static final String MONTH = "m";
  static final String YEAR = "y";
  static final String DATE_ATTRIBUTE_POSITION = "1";
  static final String AMOUNT_ATTRIBUTE_POSITION = "2";
  static final String TARGET_FORMAT_ATTRIBUTE_POSITION = "3";
  static final String MODE_ATTRIBUTE_POSITION = "4";

  @Override
  public Object process(ContentNode parent, String target, Map<String, Object> attributes) {
    String calcResult = calculate(target, attributes);

    // https://docs.asciidoctor.org/pdf-converter/latest/extend/create-converter/#override-a-method
    // https://www.rubydoc.info/gems/asciidoctor-pdf/Asciidoctor/PDF/Converter#convert_inline_quoted-instance_method
    return createPhraseNode(parent, "quoted", calcResult, Collections.emptyMap());
  }

  @Override
  public String calculate(String operation, Map<String, Object> attributes) {
    logger.log(this, "Operator on optionalDate: " + operation);
    logger.log(this, "Attributes: " + attributes);

    boolean ignoreInvalid = ignoreInvalid(attributes);
    if (ignoreInvalid) {
      logger.log(this, "Ignoring invalid attributes");
    }

    Optional<LocalDate> optionalDate = getDate(attributes, ignoreInvalid);
    if (optionalDate.isEmpty()) {
      return InvalidValue.NOT_A_DATE;
    }

    Optional<Long> optionalAmount = getAmount(attributes, ignoreInvalid);
    if (optionalAmount.isEmpty()) {
      return InvalidValue.NOT_A_NUMBER;
    }

    String rawTargetFormat = getRawTargetFormat(attributes);
    Optional<DateTimeFormatter> optionalTargetFormat = getTargetFormat(rawTargetFormat, ignoreInvalid);
    if (!ignoreInvalid && optionalTargetFormat.isEmpty() && rawTargetFormat != null) {
      return InvalidValue.NOT_A_FORMAT;
    }

    DateTimeFormatter formatter = optionalTargetFormat.orElse(DateTimeFormatter.ISO_LOCAL_DATE);
    TemporalUnit unit = getTemporalUnit(attributes);

    LocalDate date = optionalDate.get();
    long amount = optionalAmount.get();
    logger.log(this, "Performing " + operation
                     + " on date: " + date
                     + " with amount: " + amount
                     + " and formatter: " + formatter);

    if (operation.equals(Operator.SUM)) {
      return formatter.format(date.plus(amount, unit));
    }
    if (operation.equals(Operator.SUB)) {
      return formatter.format(date.minus(amount, unit));
    }
    return InvalidValue.NOT_AN_OPERATION;
  }

  private static boolean ignoreInvalid(Map<String, Object> attributes) {
    Object mode = attributes.getOrDefault(Macro.Key.MODE, attributes.get(MODE_ATTRIBUTE_POSITION));
    return mode != null && Macro.Value.IGNORE_INVALID.equals(String.valueOf(mode));
  }

  private Optional<LocalDate> getDate(Map<String, Object> attributes, boolean ignoreInvalid) {
    Object dateValue = attributes.getOrDefault(Macro.Key.DATE,
                                               attributes.get(DATE_ATTRIBUTE_POSITION));
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
    Object value = attr.getOrDefault(Macro.Key.VALUE, attr.get(AMOUNT_ATTRIBUTE_POSITION));
    String valueString = String.valueOf(value);
    if (valueString.endsWith(DAY) || valueString.endsWith(MONTH) || valueString.endsWith(YEAR)) {
      String rawValue = valueString.substring(0, valueString.length() - 1);

      return getLong(rawValue, ignoreInvalid);
    }
    return getLong(valueString, ignoreInvalid);
  }

  private String getRawTargetFormat(Map<String, Object> attr) {
    Object value = attr.getOrDefault(Macro.Key.TARGET_FORMAT,
                                     attr.get(TARGET_FORMAT_ATTRIBUTE_POSITION));
    logger.log(this, "Raw format: " + value);
    return value != null ? value.toString() : null;
  }

  private Optional<DateTimeFormatter> getTargetFormat(String rawFormat, boolean ignoreInvalid) {
    DateTimeFormatter dateTimeFormatter = null;
    try {
      dateTimeFormatter = DateTimeFormatter.ofPattern(rawFormat, Locale.ENGLISH);
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
    Object value = attributes.getOrDefault(Macro.Key.VALUE,
                                           attributes.get(AMOUNT_ATTRIBUTE_POSITION));
    String valueString = String.valueOf(value);
    String rawValue = DAY;
    if (valueString.endsWith(DAY) || valueString.endsWith(MONTH) || valueString.endsWith(YEAR)) {
      rawValue = valueString.substring(valueString.length() - 1);
    }
    switch (rawValue) {
      case YEAR:
        return ChronoUnit.YEARS;
      case MONTH:
        return ChronoUnit.MONTHS;
      case DAY:
      default:
        return ChronoUnit.DAYS;
    }
  }
}
