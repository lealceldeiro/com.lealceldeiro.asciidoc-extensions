package com.lealceldeiro.asciidoc.extensions.chart;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LineChartSvgTest {
  private static BigDecimal bd(String s) {
    return new BigDecimal(s);
  }

  @Test
  void rendersWellFormedSvgWithAxesLabelsAndSeriesLine() {
    List<String> x = List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun");
    List<ChartSeries> series = List.of(
        new ChartSeries("Family", Arrays.asList(null, null, null, bd("5568.09"), bd("5362.59"), bd("5633.96"))));
    String svg = LineChartSvg.render(x, series, ChartOptions.defaults());

    Assertions.assertTrue(svg.startsWith("<svg"), "starts with <svg");
    Assertions.assertTrue(svg.trim().endsWith("</svg>"), "ends with </svg>");
    Assertions.assertTrue(svg.contains(">Jan<") && svg.contains(">Jun<"), "has x labels");
    Assertions.assertTrue(svg.contains(">Family<"), "has legend entry");
    Assertions.assertTrue(svg.contains("<polyline"), "has a series polyline");
    Assertions.assertTrue(svg.contains(LineChartSvg.PALETTE.get(0)), "uses palette color 0");
  }

  @Test
  void gapsSplitTheLineIntoSeparateSegments() {
    // present, gap, present, present -> two polyline segments (one point cannot form a segment)
    List<String> x = List.of("Jan", "Feb", "Mar", "Apr");
    List<ChartSeries> series = List.of(
        new ChartSeries("S", Arrays.asList(bd("10"), null, bd("30"), bd("40"))));
    String svg = LineChartSvg.render(x, series, ChartOptions.defaults());
    int polylineCount = svg.split("<polyline", -1).length - 1;
    Assertions.assertEquals(1, polylineCount, "only the Mar-Apr pair forms a segment");
  }

  @Test
  void honorsExplicitBoundsAndUnitSuffix() {
    List<String> x = List.of("Jan", "Feb");
    List<ChartSeries> series = List.of(new ChartSeries("S", Arrays.asList(bd("1"), bd("2"))));
    ChartOptions opts = new ChartOptions(400, 200, bd("0"), bd("10"), "€", true);
    String svg = LineChartSvg.render(x, series, opts);
    Assertions.assertTrue(svg.contains("width=\"400\""), "width honored");
    Assertions.assertTrue(svg.contains("10€") || svg.contains("10 €"), "top tick has unit");
  }
}
