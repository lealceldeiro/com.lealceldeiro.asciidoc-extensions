package com.lealceldeiro.asciidoc.extensions.chart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
  void loneLeadingPointBeforeAGapIsDroppedLeavingOneSegment() {
    // lone present point, gap, present, present -> the lone point can't form a
    // segment by itself, so only the Mar-Apr pair renders: one polyline.
    List<String> x = List.of("Jan", "Feb", "Mar", "Apr");
    List<ChartSeries> series = List.of(
        new ChartSeries("S", Arrays.asList(bd("10"), null, bd("30"), bd("40"))));
    String svg = LineChartSvg.render(x, series, ChartOptions.defaults());
    int polylineCount = svg.split("<polyline", -1).length - 1;
    Assertions.assertEquals(1, polylineCount, "only the Mar-Apr pair forms a segment");
  }

  @Test
  void midSeriesGapSplitsARunIntoTwoSegments() {
    // two present points, gap, two present points -> both runs have >= 2 points,
    // so the gap genuinely splits the line into two separate polylines.
    List<String> x = List.of("Jan", "Feb", "Mar", "Apr", "May");
    List<ChartSeries> series = List.of(
        new ChartSeries("S",
            Arrays.asList(bd("10"), bd("20"), null, bd("40"), bd("50"))));
    String svg = LineChartSvg.render(x, series, ChartOptions.defaults());
    int polylineCount = svg.split("<polyline", -1).length - 1;
    Assertions.assertEquals(2, polylineCount, "Jan-Feb and Apr-May each form a segment");
  }

  @Test
  void legendWrapsToMultipleRowsAndStaysWithinChartWidthForManySeries() {
    List<String> x = List.of("Jan");
    List<ChartSeries> series = new ArrayList<>();
    List<String> names = List.of("Alpha", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot");
    for (int i = 0; i < names.size(); i++) {
      series.add(new ChartSeries(names.get(i), Arrays.asList(bd(String.valueOf(i + 1)))));
    }
    ChartOptions opts = ChartOptions.defaults();
    String svg = LineChartSvg.render(x, series, opts);

    for (String name : names) {
      Assertions.assertTrue(svg.contains(">" + name + "<"), "missing legend entry: " + name);
    }

    // Legend swatches are the only <rect> elements with width="11" height="11";
    // legend labels are the only <text> elements with font-size="11". Neither
    // should ever be positioned beyond the SVG's own width.
    List<Double> legendXs = new ArrayList<>();
    int legendSwatchCount = 0;
    Matcher rectM = Pattern.compile(
        "<rect x=\"(-?[0-9.]+)\" y=\"[^\"]*\" width=\"11\" height=\"11\"").matcher(svg);
    while (rectM.find()) {
      legendXs.add(Double.parseDouble(rectM.group(1)));
      legendSwatchCount++;
    }
    Matcher textM = Pattern.compile(
        "<text x=\"(-?[0-9.]+)\"[^>]*font-size=\"11\"").matcher(svg);
    while (textM.find()) {
      legendXs.add(Double.parseDouble(textM.group(1)));
    }

    Assertions.assertEquals(names.size(), legendSwatchCount, "one swatch per series");
    Assertions.assertFalse(legendXs.isEmpty(), "expected legend coordinates to be found");
    for (double legendX : legendXs) {
      Assertions.assertTrue(legendX <= opts.width(),
          "legend x=" + legendX + " overflows chart width=" + opts.width());
    }
  }

  @Test
  void honorsExplicitBoundsAndUnitSuffix() {
    List<String> x = List.of("Jan", "Feb");
    List<ChartSeries> series = List.of(new ChartSeries("S", Arrays.asList(bd("1"), bd("2"))));
    ChartOptions opts = new ChartOptions(400, 200, bd("0"), bd("10"), "€", true, "none");
    String svg = LineChartSvg.render(x, series, opts);
    Assertions.assertTrue(svg.contains("width=\"400\""), "width honored");
    Assertions.assertTrue(svg.contains("10€") || svg.contains("10 €"), "top tick has unit");
  }

  @Test
  void defaultDoesNotRenderPointValueLabels() {
    List<String> x = List.of("Apr", "May", "Jun");
    List<ChartSeries> series = List.of(
        new ChartSeries("Family", Arrays.asList(bd("5568.09"), bd("5362.59"), bd("5633.96"))));
    String svg = LineChartSvg.render(x, series, ChartOptions.defaults());
    Assertions.assertFalse(svg.contains(">5568.09<"), "no exact-value label by default");
    Assertions.assertFalse(svg.contains("~5.57K"), "no k-notation label by default");
  }

  @Test
  void fullPointLabelsShowExactAmounts() {
    List<String> x = List.of("Apr", "May", "Jun");
    List<ChartSeries> series = List.of(
        new ChartSeries("Work", Arrays.asList(bd("357.49"), bd("354.00"), bd("353.75"))));
    ChartOptions opts = new ChartOptions(520, 300, null, null, null, true, "full");
    String svg = LineChartSvg.render(x, series, opts);
    Assertions.assertTrue(svg.contains(">357.49<"), "exact Apr amount labelled");
    Assertions.assertTrue(svg.contains(">353.75<"), "exact Jun amount labelled");
  }

  @Test
  void kPointLabelsApproximateThousandsAndKeepSmallValuesFull() {
    List<String> x = List.of("Apr", "May", "Jun");
    List<ChartSeries> family = List.of(
        new ChartSeries("Family", Arrays.asList(bd("5568.09"), bd("5362.59"), bd("5633.96"))));
    ChartOptions opts = new ChartOptions(520, 300, null, null, null, true, "k");
    String svg = LineChartSvg.render(x, family, opts);
    Assertions.assertTrue(svg.contains(">~5.57K<"), "5568.09 -> ~5.57K");
    Assertions.assertTrue(svg.contains(">~5.36K<"), "5362.59 -> ~5.36K");
    Assertions.assertTrue(svg.contains(">~5.63K<"), "5633.96 -> ~5.63K");

    // values below 1000 stay in full even in k mode
    List<ChartSeries> work = List.of(new ChartSeries("Work", Arrays.asList(bd("357.49"))));
    String workSvg = LineChartSvg.render(List.of("Apr"), work, opts);
    Assertions.assertTrue(workSvg.contains(">357.49<"), "357.49 stays full (< 1000)");
    Assertions.assertFalse(workSvg.contains("K<"), "no K suffix for sub-1000 value");
  }
}
