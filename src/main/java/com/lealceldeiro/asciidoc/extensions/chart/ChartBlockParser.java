package com.lealceldeiro.asciidoc.extensions.chart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses a chart block body (and block attributes) into a {@link ParsedChart}. */
public final class ChartBlockParser {
  private static final Pattern ATTR_REF = Pattern.compile("\\{([A-Za-z0-9_][A-Za-z0-9_-]*)}");

  private ChartBlockParser() {
  }

  public static String substituteAttributes(String line, Map<String, Object> docAttributes) {
    Matcher m = ATTR_REF.matcher(line);
    StringBuilder out = new StringBuilder();
    while (m.find()) {
      Object value = docAttributes.get(m.group(1));
      m.appendReplacement(out, Matcher.quoteReplacement(value == null ? "" : String.valueOf(value)));
    }
    m.appendTail(out);
    return out.toString();
  }

  public static ParsedChart parse(List<String> rawLines, Map<String, Object> docAttributes,
                                  Map<String, Object> blockAttributes) {
    List<String> xLabels = new ArrayList<>();
    List<ChartSeries> series = new ArrayList<>();

    for (String raw : rawLines) {
      String line = substituteAttributes(raw, docAttributes).trim();
      if (line.isEmpty() || line.startsWith("//")) {
        continue;
      }
      int colon = line.indexOf(':');
      if (colon < 0) {
        continue;
      }
      String key = line.substring(0, colon).trim();
      String rest = line.substring(colon + 1).trim();
      if (key.equalsIgnoreCase("x") || key.equalsIgnoreCase("labels")) {
        xLabels.clear();
        for (String label : rest.split(",", -1)) {
          xLabels.add(label.trim());
        }
      } else {
        series.add(new ChartSeries(key, parseValues(rest)));
      }
    }
    return new ParsedChart(xLabels, series, readOptions(blockAttributes));
  }

  private static List<BigDecimal> parseValues(String csv) {
    List<BigDecimal> values = new ArrayList<>();
    for (String cell : csv.split(",", -1)) {
      String s = cell.trim();
      if (s.isEmpty()) {
        values.add(null);
        continue;
      }
      try {
        values.add(new BigDecimal(s));
      } catch (NumberFormatException e) {
        values.add(null);
      }
    }
    return values;
  }

  private static ChartOptions readOptions(Map<String, Object> attrs) {
    ChartOptions d = ChartOptions.defaults();
    int width = intAttr(attrs, "width", d.width());
    int height = intAttr(attrs, "height", d.height());
    BigDecimal yMin = decimalAttr(attrs, "ymin");
    BigDecimal yMax = decimalAttr(attrs, "ymax");
    String unit = strAttr(attrs, "unit", d.unit());
    boolean points = !"false".equalsIgnoreCase(strAttr(attrs, "points", "true"));
    return new ChartOptions(width, height, yMin, yMax, unit, points);
  }

  private static int intAttr(Map<String, Object> attrs, String key, int fallback) {
    Object v = attrs.get(key);
    if (v == null) {
      return fallback;
    }
    try {
      return Integer.parseInt(String.valueOf(v).trim());
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  private static BigDecimal decimalAttr(Map<String, Object> attrs, String key) {
    Object v = attrs.get(key);
    if (v == null) {
      return null;
    }
    try {
      return new BigDecimal(String.valueOf(v).trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static String strAttr(Map<String, Object> attrs, String key, String fallback) {
    Object v = attrs.get(key);
    return v == null ? fallback : String.valueOf(v);
  }
}
