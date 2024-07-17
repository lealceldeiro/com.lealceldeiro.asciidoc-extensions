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
public class CalcExpressionMacro extends InlineMacroProcessor {
  public static final String NOT_AN_EXPRESSION = "NaE";

  @Override
  public Object process(ContentNode parent, String target, Map<String, Object> attributes) {
    confirmXParserLicence();
    String calcResult = evaluate(target);

    // https://docs.asciidoctor.org/pdf-converter/latest/extend/create-converter/#override-a-method
    // https://www.rubydoc.info/gems/asciidoctor-pdf/Asciidoctor/PDF/Converter#convert_inline_quoted-instance_method
    return createPhraseNode(parent, "quoted", calcResult, Collections.emptyMap());
  }

  private void confirmXParserLicence() {
    License.iConfirmNonCommercialUse("asiel@lealceldeiro.com");
  }

  private String evaluate(String expression) {
    logDebug("Expression: " + expression);

    return evalExpression(expression).map(BigDecimal::new)
                                     .map(value -> value.setScale(2, RoundingMode.CEILING))
                                     .map(BigDecimal::toString)
                                     .orElse(NOT_AN_EXPRESSION);
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
      return Optional.empty();
    }
    return Optional.of(expResult);
  }
}
