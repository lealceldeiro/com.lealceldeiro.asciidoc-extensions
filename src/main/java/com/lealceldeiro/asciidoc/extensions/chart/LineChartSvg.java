package com.lealceldeiro.asciidoc.extensions.chart;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/** Pure, deterministic SVG line-chart generator. No Asciidoctor dependencies. */
public final class LineChartSvg {
  public static final List<String> PALETTE = List.of(
      "#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd",
      "#8c564b", "#e377c2", "#7f7f7f", "#bcbd22", "#17becf");

  private static final int PAD_LEFT = 55;
  private static final int PAD_RIGHT = 20;
  private static final int PAD_TOP = 24;
  private static final int PAD_BOTTOM = 40;
  private static final int TARGET_TICKS = 4;
  private static final BigDecimal K_THRESHOLD = new BigDecimal("1000");
  private static final int LEGEND_COLUMNS = 4;
  private static final double LEGEND_COLUMN_WIDTH = 80.0;
  private static final double LEGEND_ROW_HEIGHT = 14.0;

  private LineChartSvg() {
  }

  public static String render(List<String> xLabels, List<ChartSeries> series,
                              ChartOptions options) {
    int w = options.width();
    int h = options.height();
    double plotLeft = PAD_LEFT;
    double plotRight = w - PAD_RIGHT;
    double plotTop = PAD_TOP;
    double plotBottom = h - PAD_BOTTOM;

    AxisScale scale = resolveScale(series, options);
    List<Double> ticks = scale.ticks();

    StringBuilder svg = new StringBuilder();
    svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(w)
       .append("\" height=\"").append(h).append("\" viewBox=\"0 0 ").append(w).append(' ')
       .append(h).append("\" font-family=\"Helvetica, Arial, sans-serif\">\n");
    svg.append("<rect x=\"0\" y=\"0\" width=\"").append(w).append("\" height=\"").append(h)
       .append("\" fill=\"#ffffff\"/>\n");

    // gridlines + y tick labels
    for (double t : ticks) {
      double y = yFor(t, scale, plotTop, plotBottom);
      svg.append("<line x1=\"").append(fmt(plotLeft)).append("\" y1=\"").append(fmt(y))
         .append("\" x2=\"").append(fmt(plotRight)).append("\" y2=\"").append(fmt(y))
         .append("\" stroke=\"#e0e0e0\" stroke-width=\"1\"/>\n");
      svg.append("<text x=\"").append(fmt(plotLeft - 6)).append("\" y=\"").append(fmt(y + 3))
         .append("\" font-size=\"10\" fill=\"#555\" text-anchor=\"end\">")
         .append(escape(formatTick(t, options.unit()))).append("</text>\n");
    }

    // axes
    svg.append("<line x1=\"").append(fmt(plotLeft)).append("\" y1=\"").append(fmt(plotTop))
       .append("\" x2=\"").append(fmt(plotLeft)).append("\" y2=\"").append(fmt(plotBottom))
       .append("\" stroke=\"#333\" stroke-width=\"1.5\"/>\n");
    svg.append("<line x1=\"").append(fmt(plotLeft)).append("\" y1=\"").append(fmt(plotBottom))
       .append("\" x2=\"").append(fmt(plotRight)).append("\" y2=\"").append(fmt(plotBottom))
       .append("\" stroke=\"#333\" stroke-width=\"1.5\"/>\n");

    // x labels
    int n = xLabels.size();
    for (int i = 0; i < n; i++) {
      double x = xFor(i, n, plotLeft, plotRight);
      svg.append("<text x=\"").append(fmt(x)).append("\" y=\"").append(fmt(plotBottom + 16))
         .append("\" font-size=\"10\" fill=\"#555\" text-anchor=\"middle\">")
         .append(escape(xLabels.get(i))).append("</text>\n");
    }

    // series
    for (int s = 0; s < series.size(); s++) {
      ChartSeries cs = series.get(s);
      String color = PALETTE.get(s % PALETTE.size());
      appendSeries(svg, cs, color, n, scale, plotLeft, plotRight, plotTop, plotBottom,
                   options.points(), options.pointLabels());
    }

    // legend (top-right, inside plot)
    appendLegend(svg, series, plotRight);

    svg.append("</svg>\n");
    return svg.toString();
  }

  private static AxisScale resolveScale(List<ChartSeries> series, ChartOptions options) {
    if (options.yMin() != null && options.yMax() != null) {
      double lo = options.yMin().doubleValue();
      double hi = options.yMax().doubleValue();
      return AxisScale.nice(lo, hi, TARGET_TICKS);
    }
    double dataMin = 0;
    double dataMax = 1;
    boolean any = false;
    for (ChartSeries cs : series) {
      for (BigDecimal v : cs.values()) {
        if (v == null) {
          continue;
        }
        double d = v.doubleValue();
        if (!any) {
          dataMin = d;
          dataMax = d;
          any = true;
        } else {
          dataMin = Math.min(dataMin, d);
          dataMax = Math.max(dataMax, d);
        }
      }
    }
    double lo = options.yMin() != null ? options.yMin().doubleValue() : dataMin;
    double hi = options.yMax() != null ? options.yMax().doubleValue() : dataMax;
    return AxisScale.nice(lo, hi, TARGET_TICKS);
  }

  private static void appendSeries(StringBuilder svg, ChartSeries cs, String color, int n,
                                   AxisScale scale, double plotLeft, double plotRight,
                                   double plotTop, double plotBottom, boolean points,
                                   String pointLabels) {
    List<double[]> current = new ArrayList<>();
    List<double[]> allPoints = new ArrayList<>();
    List<BigDecimal> allValues = new ArrayList<>();
    List<List<double[]>> segments = new ArrayList<>();
    int count = Math.min(n, cs.values().size());
    for (int i = 0; i < count; i++) {
      BigDecimal v = cs.values().get(i);
      if (v == null) {
        if (current.size() >= 2) {
          segments.add(current);
        }
        current = new ArrayList<>();
        continue;
      }
      double x = xFor(i, n, plotLeft, plotRight);
      double y = yFor(v.doubleValue(), scale, plotTop, plotBottom);
      current.add(new double[] {x, y});
      allPoints.add(new double[] {x, y});
      allValues.add(v);
    }
    if (current.size() >= 2) {
      segments.add(current);
    }
    for (List<double[]> seg : segments) {
      StringBuilder pts = new StringBuilder();
      for (double[] p : seg) {
        pts.append(fmt(p[0])).append(',').append(fmt(p[1])).append(' ');
      }
      svg.append("<polyline fill=\"none\" stroke=\"").append(color)
         .append("\" stroke-width=\"2\" points=\"").append(pts.toString().trim())
         .append("\"/>\n");
    }
    if (points) {
      for (double[] p : allPoints) {
        svg.append("<circle cx=\"").append(fmt(p[0])).append("\" cy=\"").append(fmt(p[1]))
           .append("\" r=\"3\" fill=\"").append(color).append("\"/>\n");
      }
    }
    if (pointLabels != null && !"none".equalsIgnoreCase(pointLabels)) {
      for (int i = 0; i < allPoints.size(); i++) {
        double[] p = allPoints.get(i);
        // sit the label above the point, but drop it below when it would clip the top edge
        double labelY = (p[1] - 7 < plotTop + 8) ? p[1] + 14 : p[1] - 7;
        svg.append("<text x=\"").append(fmt(p[0])).append("\" y=\"").append(fmt(labelY))
           .append("\" font-size=\"9\" fill=\"#333\" text-anchor=\"middle\">")
           .append(escape(formatPointLabel(allValues.get(i), pointLabels)))
           .append("</text>\n");
      }
    }
  }

  private static String formatPointLabel(BigDecimal value, String mode) {
    if ("k".equalsIgnoreCase(mode) && value.abs().compareTo(K_THRESHOLD) >= 0) {
      return "~" + value.movePointLeft(3).setScale(2, RoundingMode.HALF_UP).toPlainString() + "K";
    }
    return value.toPlainString();
  }

  private static void appendLegend(StringBuilder svg, List<ChartSeries> series, double plotRight) {
    // Lay entries left-to-right in rows of at most LEGEND_COLUMNS, wrapping to a
    // new row (stacked downward) instead of running off the right edge for a
    // 5th+ series. Each row ends near the right edge of the plot.
    int columns = Math.min(series.size(), LEGEND_COLUMNS);
    double startX = plotRight - columns * LEGEND_COLUMN_WIDTH;
    if (startX < 60) {
      startX = 60;
    }
    for (int s = 0; s < series.size(); s++) {
      int col = s % LEGEND_COLUMNS;
      int row = s / LEGEND_COLUMNS;
      double lx = startX + col * LEGEND_COLUMN_WIDTH;
      double ly = 12 + row * LEGEND_ROW_HEIGHT;
      String color = PALETTE.get(s % PALETTE.size());
      svg.append("<rect x=\"").append(fmt(lx)).append("\" y=\"").append(fmt(ly))
         .append("\" width=\"11\" height=\"11\" fill=\"").append(color).append("\"/>\n");
      svg.append("<text x=\"").append(fmt(lx + 15)).append("\" y=\"").append(fmt(ly + 10))
         .append("\" font-size=\"11\" fill=\"#333\">").append(escape(series.get(s).name()))
         .append("</text>\n");
    }
  }

  private static double xFor(int i, int n, double plotLeft, double plotRight) {
    if (n <= 1) {
      return (plotLeft + plotRight) / 2;
    }
    return plotLeft + (plotRight - plotLeft) * i / (n - 1);
  }

  private static double yFor(double value, AxisScale scale, double plotTop, double plotBottom) {
    double range = scale.max() - scale.min();
    if (range <= 0) {
      return plotBottom;
    }
    return plotBottom - (value - scale.min()) / range * (plotBottom - plotTop);
  }

  private static String formatTick(double v, String unit) {
    String num;
    if (Math.abs(v - Math.rint(v)) < 1e-9) {
      num = Long.toString(Math.round(v));
    } else {
      num = String.valueOf(Math.round(v * 100) / 100.0);
    }
    return (unit == null || unit.isEmpty()) ? num : num + unit;
  }

  private static String fmt(double d) {
    if (Math.abs(d - Math.rint(d)) < 1e-9) {
      return Long.toString(Math.round(d));
    }
    return String.valueOf(Math.round(d * 100) / 100.0);
  }

  private static String escape(String s) {
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
