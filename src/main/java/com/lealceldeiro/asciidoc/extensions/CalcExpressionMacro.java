package com.lealceldeiro.asciidoc.extensions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.extension.Format;
import org.asciidoctor.extension.FormatType;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Name;
import org.asciidoctor.extension.PositionalAttributes;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.License;

/**
 * Docs at
 * <a href="https://docs.asciidoctor.org/asciidoctorj/latest/extensions/extensions-introduction/">
 * AsciidocJ Extensions API
 * </a>
 */
@Name("calc_exp")
@Format(FormatType.SHORT)
@PositionalAttributes({"exp", "author", "calc_exp_license_type"})
public class CalcExpressionMacro extends InlineMacroProcessor {
  public static final String EXP_KEY = "exp";

  public static final String NOT_AN_EXPRESSION = "NaE";
  public static final String NOT_AN_AUTHOR = "NaA";
  public static final String NOT_A_VALID_AUTHOR = "NaVA";
  public static final String NOT_A_LICENSE = "NaL";

  public static final String AUTHOR_KEY = "author";
  public static final String LICENSE_TYPE_KEY = "calc_exp_license_type";
  public static final String LICENSE_TYPE_COMMERCIAL_VALUE = "commercial";
  public static final String LICENSE_TYPE_NON_COMMERCIAL_VALUE = "non_commercial";

  @Override
  public Object process(ContentNode parent, String target, Map<String, Object> attributes) {
    logDebug("Attributes: " + attributes);

    String expression = getExpression(attributes);
    if (expression == null || expression.isBlank()) {
      return NOT_AN_EXPRESSION;
    }

    String author = getAttribute(AUTHOR_KEY, parent, attributes);
    if (author == null) {
      return NOT_AN_AUTHOR;
    }
    if (author.length() < 5) {
      return NOT_A_VALID_AUTHOR;
    }

    String licenseType = getAttribute(LICENSE_TYPE_KEY, parent, attributes,
                                      LICENSE_TYPE_COMMERCIAL_VALUE, LICENSE_TYPE_NON_COMMERCIAL_VALUE);
    if (licenseType == null) {
      return NOT_A_LICENSE;
    }

    confirmXParserLicence(author, licenseType);
    String calcResult = evaluate(expression);

    // https://docs.asciidoctor.org/pdf-converter/latest/extend/create-converter/#override-a-method
    // https://www.rubydoc.info/gems/asciidoctor-pdf/Asciidoctor/PDF/Converter#convert_inline_quoted-instance_method
    return createPhraseNode(parent, "quoted", calcResult, Collections.emptyMap());
  }

  private static String getAttribute(String attrName, ContentNode parent, Map<String, Object> attrs,
                                     String... validValues) {
    Object rawAttr = attrs.containsKey(attrName)
                     ? attrs.get(attrName)
                     : parent.getDocument().getAttribute(attrName);
    String attr = String.valueOf(rawAttr);

    if (attr == null || attr.isBlank()) {
      return null;
    }

    boolean valid = validValues.length == 0;
    for (String validValue : validValues) {
      if (attr.equals(validValue)) {
        return attr;
      }
    }

    return valid ? attr : null;
  }

  private void confirmXParserLicence(String author, String licenseType) {
    if (LICENSE_TYPE_NON_COMMERCIAL_VALUE.equals(licenseType)) {
      License.iConfirmNonCommercialUse(author);
    } else if (LICENSE_TYPE_COMMERCIAL_VALUE.equals(licenseType)) {
      License.iConfirmCommercialUse(author);
    } else {
      logDebug("Unknown license type: " + licenseType);
    }
  }

  private String evaluate(String expression) {
    logDebug("Expression: " + expression);

    return evalExpression(expression).map(BigDecimal::new)
                                     .map(value -> value.setScale(2, RoundingMode.CEILING))
                                     .map(BigDecimal::toString)
                                     .orElse(NOT_AN_EXPRESSION);
  }

  private String getExpression(Map<String, Object> attrs) {
    Object val = attrs.getOrDefault(EXP_KEY, attrs.get("1"));
    return val != null ? String.valueOf(val) : null;
  }

  private void logDebug(String message) {
    log(new LogRecord(Severity.DEBUG, message));
  }

  /**
   * Evaluates an expression and returns the result of the evaluation as a string value.
   *
   * @param expression Expression to be evaluated.
   *
   * @return Evaluation of the expression.
   *
   * @see <a href="https://mathparser.org/">mXParser</a>
   */
  private Optional<Double> evalExpression(String expression) {
    Expression exp = new Expression(expression);
    double expResult = exp.calculate();
    if (Double.isNaN(expResult)) {
      logDebug(expResult + " returned from math lib for expression: " + expression
               + ", evaluated: " + exp);
      return Optional.empty();
    }
    return Optional.of(expResult);
  }
}
