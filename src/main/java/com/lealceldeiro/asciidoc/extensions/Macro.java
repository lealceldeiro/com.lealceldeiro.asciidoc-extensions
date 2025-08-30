package com.lealceldeiro.asciidoc.extensions;

public final class Macro {
  private Macro() {
  }

  public static final class Key {
    private Key() {
    }

    public static final String MODE = "mode";
    public static final String DATE = "date";
    public static final String VALUE = "value";
    public static final String TARGET_FORMAT = "format";
    public static final String FROM_ZONE_ID = "from_zone_id";
    public static final String TO_ZONE_ID = "to_zone_id";

    public static final String EXP = "exp";
    public static final String AUTHOR = "author";
    public static final String LICENSE_TYPE = "calc_exp_license_type";
  }

  public static final class Value {
    private Value() {
    }

    public static final String IGNORE_INVALID = "ignore_invalid";
  }
}
