package com.lealceldeiro.asciidoc.extensions;

import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLogger;
import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLoggerFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;

public final class Util {
  private static final ExtensionLogger logger = ExtensionLoggerFactory.getInstance();

  private Util() {
  }

  /**
   * Resolves the phrase-node attributes that carry the role to apply to a calculated result,
   * based on the sign of that result.
   *
   * <p>The role names themselves are supplied by the caller (they are opaque to this method):
   * {@link Macro.Key#ROLE_POSITIVE} when the result is greater than zero,
   * {@link Macro.Key#ROLE_NEGATIVE} when it is less than zero, and
   * {@link Macro.Key#ROLE_ZERO} when it equals zero. When the relevant role is not provided
   * (or is blank), or when {@code result} is not a number, no role is applied.</p>
   *
   * @param result          the calculated result, as produced by a calc macro.
   * @param macroAttributes the macro attributes possibly holding the sign-based role names.
   *
   * @return a single-entry {@code {"role": <name>}} map, or an empty map when no role applies.
   */
  public static Map<String, Object> signRoleAttributes(String result,
                                                       Map<String, Object> macroAttributes) {
    int signum;
    try {
      signum = new BigDecimal(result).signum();
    } catch (NullPointerException | NumberFormatException e) {
      return Collections.emptyMap();
    }

    String roleKey = signum > 0 ? Macro.Key.ROLE_POSITIVE
                                : signum < 0 ? Macro.Key.ROLE_NEGATIVE
                                             : Macro.Key.ROLE_ZERO;

    Object role = macroAttributes.get(roleKey);
    if (role == null) {
      return Collections.emptyMap();
    }
    String roleName = String.valueOf(role).trim();

    return roleName.isEmpty() ? Collections.emptyMap() : Map.of("role", roleName);
  }

  public static RoundingMode roundingMode(org.asciidoctor.extension.BaseProcessor callingProcessor,
                                          Map<String, Object> attributes) {
    Object specifiedRoundingMode = attributes.get(Macro.Key.ROUNDING_MODE);
    if (specifiedRoundingMode instanceof String modeString) {
      try {
        return RoundingMode.valueOf(modeString);
      } catch (IllegalArgumentException e) {
        logger.log(callingProcessor, "Invalid rounding mode: " + modeString);
      }
    } else {
      logger.log(callingProcessor, "Rounding mode not set");
    }

    return RoundingMode.HALF_EVEN;
  }
}
