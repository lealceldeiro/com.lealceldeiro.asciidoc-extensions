package com.lealceldeiro.asciidoc.extensions.calc;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CalcMacroIntegrationTest {
  private static String convert(String macro) {
    String doc = String.join("\n", "", macro, "");
    try (Asciidoctor asciidoctor = Asciidoctor.Factory.create()) {
      return asciidoctor.convert(doc,
          Options.builder().safe(SafeMode.UNSAFE).toFile(false).build());
    }
  }

  @Test
  void positiveSumRendersPositiveRole() {
    String html = convert("calc:sum[100, 50, role_positive=success-bg, role_negative=danger-bg]");
    Assertions.assertTrue(html.contains("150.00"), html);
    Assertions.assertTrue(html.contains("success-bg"), html);
    Assertions.assertFalse(html.contains("danger-bg"), html);
  }

  @Test
  void negativeSubtractionRendersNegativeRole() {
    String html = convert("calc:sub[50, 100, role_positive=success-bg, role_negative=danger-bg]");
    Assertions.assertTrue(html.contains("-50.00"), html);
    Assertions.assertTrue(html.contains("danger-bg"), html);
    Assertions.assertFalse(html.contains("success-bg"), html);
  }

  @Test
  void noRoleAttributesRendersNoRoleAndStillSums() {
    String html = convert("calc:sum[100, 50]");
    Assertions.assertTrue(html.contains("150.00"), html);
    Assertions.assertFalse(html.contains("success-bg"), html);
    Assertions.assertFalse(html.contains("danger-bg"), html);
  }
}
