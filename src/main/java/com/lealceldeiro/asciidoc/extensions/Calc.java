package com.lealceldeiro.asciidoc.extensions;

public interface Calc<T> {
  String calculate(String target, T attributes);
}
