package com.lealceldeiro.asciidoc.extensions.chart;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChartMacroIntegrationTest {
  @Test
  void chartBlockRendersToImageReferencingGeneratedSvg() {
    String doc = String.join("\n",
        ":aprFamily: 5568.09",
        "",
        "[chart,line]",
        "----",
        "x: Jan,Feb,Mar,Apr",
        "Family: ,,,{aprFamily}",
        "----",
        "");
    try (Asciidoctor asciidoctor = Asciidoctor.Factory.create()) {
      String html = asciidoctor.convert(doc,
          Options.builder().safe(SafeMode.UNSAFE).toFile(false).build());
      Assertions.assertTrue(html.contains("adoc-chart-"), "references generated chart file");
      Assertions.assertTrue(html.contains(".svg"), "image is an svg");
    }
  }

  @Test
  void unknownChartTypeRendersNothingWithoutError() {
    String doc = String.join("\n",
        "[chart,pie]",
        "----",
        "x: A,B",
        "S: 1,2",
        "----",
        "");
    try (Asciidoctor asciidoctor = Asciidoctor.Factory.create()) {
      String html = asciidoctor.convert(doc,
          Options.builder().safe(SafeMode.UNSAFE).toFile(false).build());
      Assertions.assertFalse(html.contains("adoc-chart-"), "no chart emitted for unknown type");
    }
  }
}
