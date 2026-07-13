package com.lealceldeiro.asciidoc.extensions.chart;

import java.math.BigDecimal;

/**
 * Rendering options. {@code yMin}/{@code yMax} null = auto; {@code unit} null/empty = none;
 * {@code pointLabels} is one of {@code "none"} (no labels), {@code "full"} (exact amount) or
 * {@code "k"} (amounts &ge; 1000 shown approximate as {@code ~X.XXK}).
 */
public record ChartOptions(int width, int height, BigDecimal yMin, BigDecimal yMax,
                           String unit, boolean points, String pointLabels) {
  public static ChartOptions defaults() {
    return new ChartOptions(520, 300, null, null, null, true, "none");
  }
}
