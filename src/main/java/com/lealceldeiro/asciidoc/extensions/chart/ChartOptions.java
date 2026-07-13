package com.lealceldeiro.asciidoc.extensions.chart;

import java.math.BigDecimal;

/** Rendering options. {@code yMin}/{@code yMax} null = auto; {@code unit} null/empty = none. */
public record ChartOptions(int width, int height, BigDecimal yMin, BigDecimal yMax,
                           String unit, boolean points) {
  public static ChartOptions defaults() {
    return new ChartOptions(520, 300, null, null, null, true);
  }
}
