package com.lealceldeiro.asciidoc.extensions.chart;

import java.util.List;

public record ParsedChart(List<String> xLabels, List<ChartSeries> series, ChartOptions options) {
}
