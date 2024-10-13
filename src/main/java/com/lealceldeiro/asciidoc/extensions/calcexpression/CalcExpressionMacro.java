package com.lealceldeiro.asciidoc.extensions.calcexpression;

import static com.lealceldeiro.asciidoc.extensions.Macro.Key.AUTHOR;
import static com.lealceldeiro.asciidoc.extensions.Macro.Key.EXP;
import static com.lealceldeiro.asciidoc.extensions.Macro.Key.LICENSE_TYPE;

import com.lealceldeiro.asciidoc.extensions.Calc;
import com.lealceldeiro.asciidoc.extensions.InvalidValue;
import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLogger;
import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLoggerFactory;
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
@PositionalAttributes({EXP, AUTHOR, LICENSE_TYPE})
public class CalcExpressionMacro extends InlineMacroProcessor implements Calc<CalcExpressionMacro.Attributes> {
  public static final class Attributes {
    private final Map<String, Object> attributesAtParent;
    private final Map<String, Object> attributesAtMacro;

    public Attributes(Map<String, Object> attributesAtParent,
                      Map<String, Object> attributesAtMacro) {
      this.attributesAtParent = attributesAtParent;
      this.attributesAtMacro = attributesAtMacro;
    }

    Object getAttribute(String key) {
      return attributesAtParent.getOrDefault(key, attributesAtMacro.get(key));
    }

    @Override
    public String toString() {
      return "Attributes{" +
             "attributesAtParent=" + attributesAtParent +
             ", attributesAtMacro=" + attributesAtMacro +
             '}';
    }
  }

  private static final ExtensionLogger logger = ExtensionLoggerFactory.getInstance();
  static final String EXP_POSITION = "1";

  public static final String LICENSE_TYPE_COMMERCIAL_VALUE = "commercial";
  public static final String LICENSE_TYPE_NON_COMMERCIAL_VALUE = "non_commercial";

  @Override
  public Object process(ContentNode parent, String target, Map<String, Object> attributes) {
    Attributes attrs = getCalculationAttributes(parent, attributes);
    String result = calculate(target, attrs);

    // https://docs.asciidoctor.org/pdf-converter/latest/extend/create-converter/#override-a-method
    // https://www.rubydoc.info/gems/asciidoctor-pdf/Asciidoctor/PDF/Converter#convert_inline_quoted-instance_method
    return createPhraseNode(parent, "quoted", result, Collections.emptyMap());
  }

  private static Attributes getCalculationAttributes(ContentNode parent,
                                                     Map<String, Object> macroAttributes) {
    return new Attributes(Map.of(AUTHOR, parent.getAttribute(AUTHOR),
                                 LICENSE_TYPE, parent.getAttribute(LICENSE_TYPE)),
                          macroAttributes);
  }

  @Override
  public String calculate(String ignored, Attributes attributes) {
    logger.log(this, "Attributes: " + attributes);

    String expression = getExpression(attributes);
    if (expression == null || expression.isBlank()) {
      return InvalidValue.NOT_AN_EXPRESSION;
    }

    String author = getAttribute(AUTHOR, attributes);
    if (author == null) {
      return InvalidValue.NOT_AN_AUTHOR;
    }
    if (author.length() < 5) {
      return InvalidValue.NOT_A_VALID_AUTHOR;
    }

    String licenseType = getAttribute(LICENSE_TYPE, attributes,
                                      LICENSE_TYPE_COMMERCIAL_VALUE,
                                      LICENSE_TYPE_NON_COMMERCIAL_VALUE);
    if (licenseType == null) {
      return InvalidValue.NOT_A_LICENSE;
    }

    confirmXParserLicence(author, licenseType);
    return evaluate(expression);
  }

  private static String getAttribute(String attrName, Attributes attributes,
                                     String... validValues) {
    Object rawAttr
        = attributes.attributesAtMacro.getOrDefault(attrName,
                                                    attributes.attributesAtParent.get(attrName));
    if (rawAttr == null) {
      return null;
    }
    String attr = String.valueOf(rawAttr);
    if (attr.isBlank()) {
      return null;
    }

    boolean skipValidValuesEvaluation = validValues.length == 0;
    for (String validValue : validValues) {
      if (attr.equals(validValue)) {
        return attr;
      }
    }

    return skipValidValuesEvaluation ? attr : null;
  }

  private void confirmXParserLicence(String author, String licenseType) {
    if (LICENSE_TYPE_NON_COMMERCIAL_VALUE.equals(licenseType)) {
      License.iConfirmNonCommercialUse(author);
    } else if (LICENSE_TYPE_COMMERCIAL_VALUE.equals(licenseType)) {
      License.iConfirmCommercialUse(author);
    } else {
      logger.log(this, "Unknown license type: " + licenseType);
    }
  }

  private String evaluate(String expression) {
    logger.log(this, "Expression: " + expression);

    return evalExpression(expression).map(BigDecimal::new)
                                     .map(value -> value.setScale(2, RoundingMode.CEILING))
                                     .map(BigDecimal::toString)
                                     .orElse(InvalidValue.NOT_AN_EXPRESSION);
  }

  private String getExpression(Attributes attributes) {
    Map<String, Object> attrs = attributes.attributesAtMacro;
    Object val = attrs.getOrDefault(EXP, attrs.get(EXP_POSITION));
    return val != null ? String.valueOf(val) : null;
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
      logger.log(this, expResult + " returned from math lib for expression: " + expression
                       + ", evaluated: " + exp);
      return Optional.empty();
    }
    return Optional.of(expResult);
  }
}
