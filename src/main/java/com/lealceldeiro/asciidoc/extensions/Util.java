package com.lealceldeiro.asciidoc.extensions;

import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLogger;
import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLoggerFactory;
import java.math.RoundingMode;
import java.util.Map;

public final class Util {
  private static final ExtensionLogger logger = ExtensionLoggerFactory.getInstance();

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
