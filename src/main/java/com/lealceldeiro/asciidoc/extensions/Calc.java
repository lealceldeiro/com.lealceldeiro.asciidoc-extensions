package com.lealceldeiro.asciidoc.extensions;

public interface Calc<R, A1, A2> {
  R calculate(A1 a1, A2 a2);
}
