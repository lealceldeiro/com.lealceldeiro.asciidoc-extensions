package com.lealceldeiro.asciidoc.extensions.chart;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChartBlockParserTest {
  @Test
  void substitutesKnownAttributesAndBlanksUnknownOnes() {
    String out = ChartBlockParser.substituteAttributes(
        "Family: {aprFamily},{missing},3", Map.of("aprFamily", "5568.09"));
    Assertions.assertEquals("Family: 5568.09,,3", out);
  }

  @Test
  void parsesLabelsAndSparseSeriesWithGaps() {
    List<String> body = List.of(
        "x: Jan,Feb,Mar,Apr",
        "// a comment",
        "",
        "Family: ,,5568.09,5362.59",
        "Work: 10,notnum,30,40");
    ParsedChart pc = ChartBlockParser.parse(body, Map.of(), Map.of());

    Assertions.assertEquals(List.of("Jan", "Feb", "Mar", "Apr"), pc.xLabels());
    Assertions.assertEquals(2, pc.series().size());

    ChartSeries family = pc.series().get(0);
    Assertions.assertEquals("Family", family.name());
    Assertions.assertNull(family.values().get(0));
    Assertions.assertNull(family.values().get(1));
    Assertions.assertEquals(new BigDecimal("5568.09"), family.values().get(2));

    ChartSeries work = pc.series().get(1);
    Assertions.assertEquals(new BigDecimal("10"), work.values().get(0));
    Assertions.assertNull(work.values().get(1), "non-numeric -> gap");
  }

  @Test
  void readsBlockAttributesWithDefaults() {
    List<String> body = List.of("x: Jan,Feb", "S: 1,2");
    ParsedChart def = ChartBlockParser.parse(body, Map.of(), Map.of());
    Assertions.assertEquals(520, def.options().width());
    Assertions.assertTrue(def.options().points());

    ParsedChart custom = ChartBlockParser.parse(
        body, Map.of(), Map.of("width", "400", "unit", "€", "points", "false", "ymax", "9000"));
    Assertions.assertEquals(400, custom.options().width());
    Assertions.assertEquals("€", custom.options().unit());
    Assertions.assertFalse(custom.options().points());
    Assertions.assertEquals(new BigDecimal("9000"), custom.options().yMax());
  }
}
