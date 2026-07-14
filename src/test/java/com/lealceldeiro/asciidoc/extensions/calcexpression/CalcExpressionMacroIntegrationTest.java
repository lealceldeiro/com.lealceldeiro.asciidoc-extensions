package com.lealceldeiro.asciidoc.extensions.calcexpression;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CalcExpressionMacroIntegrationTest {
  private static String convert(String expression) {
    String doc = String.join("\n",
        ":author: Tester Person",
        ":calc_exp_license_type: non_commercial",
        "",
        "calc_exp:[" + expression + "]",
        "");
    try (Asciidoctor asciidoctor = Asciidoctor.Factory.create()) {
      return asciidoctor.convert(doc,
          Options.builder().safe(SafeMode.UNSAFE).toFile(false).build());
    }
  }

  @Test
  void positiveResultRendersPositiveRole() {
    String html = convert("100 - 50, role_positive=success-bg, role_negative=danger-bg");
    Assertions.assertTrue(html.contains("50.00"), html);
    Assertions.assertTrue(html.contains("success-bg"), html);
    Assertions.assertFalse(html.contains("danger-bg"), html);
  }

  @Test
  void negativeResultRendersNegativeRole() {
    String html = convert("100 - 200, role_positive=success-bg, role_negative=danger-bg");
    Assertions.assertTrue(html.contains("-100.00"), html);
    Assertions.assertTrue(html.contains("danger-bg"), html);
    Assertions.assertFalse(html.contains("success-bg"), html);
  }

  @Test
  void zeroResultRendersNoRoleWhenZeroRoleOmitted() {
    String html = convert("50 - 50, role_positive=success-bg, role_negative=danger-bg");
    Assertions.assertTrue(html.contains("0.00"), html);
    Assertions.assertFalse(html.contains("success-bg"), html);
    Assertions.assertFalse(html.contains("danger-bg"), html);
  }

  @Test
  void noRoleAttributesRendersNoRole() {
    String html = convert("100 - 50");
    Assertions.assertTrue(html.contains("50.00"), html);
    Assertions.assertFalse(html.contains("success-bg"), html);
    Assertions.assertFalse(html.contains("danger-bg"), html);
  }
}
