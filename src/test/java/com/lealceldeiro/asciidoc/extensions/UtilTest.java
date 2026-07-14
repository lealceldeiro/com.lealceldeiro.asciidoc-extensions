package com.lealceldeiro.asciidoc.extensions;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UtilTest {
  private static Map<String, Object> roles() {
    return Map.of(Macro.Key.ROLE_POSITIVE, "success-bg",
                  Macro.Key.ROLE_NEGATIVE, "danger-bg",
                  Macro.Key.ROLE_ZERO, "neutral-bg");
  }

  @Test
  void signRoleAttributesReturnsPositiveRoleForPositiveResult() {
    Assertions.assertEquals(Map.of("role", "success-bg"),
                            Util.signRoleAttributes("12.00", roles()));
  }

  @Test
  void signRoleAttributesReturnsNegativeRoleForNegativeResult() {
    Assertions.assertEquals(Map.of("role", "danger-bg"),
                            Util.signRoleAttributes("-0.01", roles()));
  }

  @Test
  void signRoleAttributesReturnsZeroRoleForZeroResult() {
    Assertions.assertEquals(Map.of("role", "neutral-bg"),
                            Util.signRoleAttributes("0.00", roles()));
  }

  @Test
  void signRoleAttributesReturnsEmptyWhenMatchingRoleAbsent() {
    // zero result but no role_zero provided -> no role
    Map<String, Object> onlyNonZero = Map.of(Macro.Key.ROLE_POSITIVE, "success-bg",
                                             Macro.Key.ROLE_NEGATIVE, "danger-bg");
    Assertions.assertEquals(Collections.emptyMap(),
                            Util.signRoleAttributes("0.00", onlyNonZero));
  }

  @Test
  void signRoleAttributesReturnsEmptyWhenNoRolesProvided() {
    Assertions.assertEquals(Collections.emptyMap(),
                            Util.signRoleAttributes("12.00", Collections.emptyMap()));
  }

  @Test
  void signRoleAttributesReturnsEmptyForBlankRoleValue() {
    Assertions.assertEquals(Collections.emptyMap(),
                            Util.signRoleAttributes("12.00", Map.of(Macro.Key.ROLE_POSITIVE, "  ")));
  }

  @Test
  void signRoleAttributesReturnsEmptyForNonNumericResult() {
    Assertions.assertEquals(Collections.emptyMap(),
                            Util.signRoleAttributes(InvalidValue.NOT_A_NUMBER, roles()));
  }

  @Test
  void signRoleAttributesReturnsEmptyForNullResult() {
    Assertions.assertEquals(Collections.emptyMap(),
                            Util.signRoleAttributes(null, roles()));
  }
}
