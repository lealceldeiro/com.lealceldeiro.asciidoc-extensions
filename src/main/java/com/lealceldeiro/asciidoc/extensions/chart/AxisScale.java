package com.lealceldeiro.asciidoc.extensions.chart;

import java.util.ArrayList;
import java.util.List;

/** Computes "nice" rounded axis bounds and evenly spaced ticks. */
public final class AxisScale {
  private final double min;
  private final double max;
  private final double step;

  private AxisScale(double min, double max, double step) {
    this.min = min;
    this.max = max;
    this.step = step;
  }

  public double min() {
    return min;
  }

  public double max() {
    return max;
  }

  public double step() {
    return step;
  }

  public List<Double> ticks() {
    List<Double> ticks = new ArrayList<>();
    // Guard against pathological steps; add a small epsilon so the top tick is included.
    for (double v = min; v <= max + step * 1e-9; v += step) {
      ticks.add(Math.round(v * 1e6) / 1e6);
    }
    return ticks;
  }

  public static AxisScale nice(double dataMin, double dataMax, int targetTicks) {
    double lo = Math.min(dataMin, dataMax);
    double hi = Math.max(dataMin, dataMax);
    if (lo > 0) {
      lo = 0; // expenses baseline at zero reads better
    }
    if (lo == hi) {
      hi = lo + 1; // avoid a zero-width range
    }
    int ticks = Math.max(1, targetTicks);
    double rawStep = (hi - lo) / ticks;
    double step = niceStep(rawStep);
    double niceMin = Math.floor(lo / step) * step;
    double niceMax = Math.ceil(hi / step) * step;
    return new AxisScale(niceMin, niceMax, step);
  }

  private static double niceStep(double rawStep) {
    double magnitude = Math.pow(10, Math.floor(Math.log10(rawStep)));
    double normalized = rawStep / magnitude;
    double factor;
    if (normalized <= 1) {
      factor = 1;
    } else if (normalized <= 2) {
      factor = 2;
    } else if (normalized <= 5) {
      factor = 5;
    } else {
      factor = 10;
    }
    return factor * magnitude;
  }
}
