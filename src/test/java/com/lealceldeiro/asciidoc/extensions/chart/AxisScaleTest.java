package com.lealceldeiro.asciidoc.extensions.chart;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AxisScaleTest {
  @Test
  void producesRoundedBoundsAndTicksForTypicalExpenseRange() {
    AxisScale s = AxisScale.nice(353.75, 5633.96, 4);
    Assertions.assertEquals(0.0, s.min(), 1e-9);
    Assertions.assertEquals(6000.0, s.max(), 1e-9);
    Assertions.assertEquals(2000.0, s.step(), 1e-9);
    Assertions.assertEquals(List.of(0.0, 2000.0, 4000.0, 6000.0), s.ticks());
  }

  @Test
  void handlesFlatDataWithoutZeroWidthRange() {
    AxisScale s = AxisScale.nice(100.0, 100.0, 4);
    Assertions.assertTrue(s.max() > s.min());
    Assertions.assertFalse(s.ticks().isEmpty());
  }
}
