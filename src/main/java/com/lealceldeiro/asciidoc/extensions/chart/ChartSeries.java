package com.lealceldeiro.asciidoc.extensions.chart;

import java.math.BigDecimal;
import java.util.List;

/** A named data series; a {@code null} value element represents a gap (no point). */
public record ChartSeries(String name, List<BigDecimal> values) {
}
