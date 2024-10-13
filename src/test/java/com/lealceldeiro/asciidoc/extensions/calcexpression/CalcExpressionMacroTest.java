package com.lealceldeiro.asciidoc.extensions.calcexpression;

import com.lealceldeiro.asciidoc.extensions.Calc;
import com.lealceldeiro.asciidoc.extensions.InvalidValue;
import com.lealceldeiro.asciidoc.extensions.Macro;
import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLogger;
import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLoggerFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.asciidoctor.ast.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mariuszgromada.math.mxparser.License;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class CalcExpressionMacroTest {
  private static final String TEST_AUTHOR = "Johny";
  private static final String TEST_LICENSE = CalcExpressionMacro.LICENSE_TYPE_NON_COMMERCIAL_VALUE;

  static Stream<Arguments> calculateSrc() {
    return Stream.of(
        Arguments.of(macroAttributes(Map.of(Macro.Key.EXP, "3 * 4")), "12.00"),
        Arguments.of(macroAttributes(Map.of(Macro.Key.EXP, "(12 * 4) / 8 + 45 * 0.5")), "28.50"),
        Arguments.of(macroAttributes(Map.of(Macro.Key.EXP, "1 / 0")),
                     InvalidValue.NOT_AN_EXPRESSION),
        Arguments.of(macroAttributes(Map.of(Macro.Key.EXP, "")),
                     InvalidValue.NOT_AN_EXPRESSION),
        Arguments.of(macroAttributes(Collections.emptyMap()),
                     InvalidValue.NOT_AN_EXPRESSION),

        Arguments.of(attributesWithoutLicense(Collections.emptyMap(),
                                              Map.of(Macro.Key.EXP, "3 * 4")),
                     InvalidValue.NOT_AN_AUTHOR),
        Arguments.of(attributesWithoutLicense(Map.of(Macro.Key.AUTHOR, TEST_AUTHOR),
                                              Map.of(Macro.Key.EXP, "3 * 4")),
                     InvalidValue.NOT_A_LICENSE),
        Arguments.of(attributesWithoutLicense(Map.of(Macro.Key.AUTHOR, "John"),
                                              Map.of(Macro.Key.EXP, "3 * 4")),
                     InvalidValue.NOT_A_VALID_AUTHOR),
        Arguments.of(attributesWithoutLicense(Map.of(Macro.Key.AUTHOR, "  "),
                                              Map.of(Macro.Key.EXP, "3 * 4")),
                     InvalidValue.NOT_AN_AUTHOR),

        Arguments.of(attributesWithoutLicense(Map.of(Macro.Key.AUTHOR,
                                                     TEST_AUTHOR,
                                                     Macro.Key.LICENSE_TYPE,
                                                     CalcExpressionMacro.LICENSE_TYPE_COMMERCIAL_VALUE),
                                              Map.of(Macro.Key.EXP, "3 * 4")), "12.00")
                    );
  }

  private static CalcExpressionMacro.Attributes macroAttributes(Map<String, Object> attributes) {
    return attributes(Collections.emptyMap(), attributes);
  }

  private static CalcExpressionMacro.Attributes attributes(Map<String, Object> parentAttributes,
                                                           Map<String, Object> macroAttributes) {
    Map<String, Object> licensedAttributes = new HashMap<>(parentAttributes);
    licensedAttributes.put(Macro.Key.AUTHOR, TEST_AUTHOR);
    licensedAttributes.put(Macro.Key.LICENSE_TYPE, TEST_LICENSE);

    return attributesWithoutLicense(licensedAttributes, macroAttributes);
  }

  private static CalcExpressionMacro.Attributes attributesWithoutLicense(Map<String, Object> parentAttributes,
                                                                         Map<String, Object> macroAttributes) {
    return new CalcExpressionMacro.Attributes(parentAttributes, macroAttributes);
  }

  @ParameterizedTest
  @MethodSource("calculateSrc")
  void calculate(CalcExpressionMacro.Attributes attributes, String expected) {

    try (MockedStatic<ExtensionLoggerFactory> elf = Mockito.mockStatic(ExtensionLoggerFactory.class);
         MockedStatic<License> license = Mockito.mockStatic(License.class)) {
      ExtensionLogger loggerMock = Mockito.mock(ExtensionLogger.class);
      elf.when(ExtensionLoggerFactory::getInstance).thenReturn(loggerMock);

      Calc<CalcExpressionMacro.Attributes> calcMacro = new CalcExpressionMacro();
      String result = calcMacro.calculate("", attributes);

      Assertions.assertEquals(expected, result);
      if (InvalidValue.NOT_AN_EXPRESSION.equals(expected)) {
        return;
      }

      Object licenseType = attributes.getAttribute(Macro.Key.LICENSE_TYPE);
      if (licenseType != null) {
        String author = String.valueOf(attributes.getAttribute(Macro.Key.AUTHOR));

        String licenseTypeString = String.valueOf(licenseType);
        if (CalcExpressionMacro.LICENSE_TYPE_COMMERCIAL_VALUE.equals(licenseTypeString)) {
          license.verify(() -> License.iConfirmCommercialUse(author), Mockito.times(1));
        } else {
          license.verify(() -> License.iConfirmNonCommercialUse(author), Mockito.times(1));
        }
      }
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {Macro.Key.AUTHOR, Macro.Key.LICENSE_TYPE})
  void getCalculationAttributesReturnsNullIfAttributeNotProvided(String attribute) {
    Document document = Mockito.mock(Document.class);
    Mockito.when(document.getAttribute(attribute)).thenReturn(null);
    CalcExpressionMacro.Attributes calculationAttributes
        = CalcExpressionMacro.getCalculationAttributes(document, null);

    Assertions.assertNull(calculationAttributes.getAttribute(attribute));
  }

  @ParameterizedTest
  @ValueSource(strings = {Macro.Key.AUTHOR, Macro.Key.LICENSE_TYPE})
  void getCalculationAttributesReturnsNullIfAttributesNotFound(String attribute) {
    Document document = Mockito.mock(Document.class);
    Mockito.when(document.getAttribute(attribute)).thenReturn(null);
    CalcExpressionMacro.Attributes calculationAttributes
        = CalcExpressionMacro.getCalculationAttributes(document, Collections.emptyMap());

    Assertions.assertNull(calculationAttributes.getAttribute(attribute));
  }

  @ParameterizedTest
  @ValueSource(strings = {Macro.Key.AUTHOR, Macro.Key.LICENSE_TYPE})
  void getCalculationAttributesReturnsValueIfAttributesProvidedAtParentDocument(String attribute) {
    String value = UUID.randomUUID().toString();

    Document document = Mockito.mock(Document.class);
    Mockito.when(document.getAttribute(attribute)).thenReturn(value);
    CalcExpressionMacro.Attributes calculationAttributes
        = CalcExpressionMacro.getCalculationAttributes(document, Collections.emptyMap());

    Assertions.assertEquals(value, calculationAttributes.getAttribute(attribute));
  }

  @ParameterizedTest
  @ValueSource(strings = {Macro.Key.AUTHOR, Macro.Key.LICENSE_TYPE})
  void getCalculationAttributesReturnsValueIfAttributesProvidedAtMacro(String attribute) {
    String value = UUID.randomUUID().toString();

    Document document = Mockito.mock(Document.class);
    CalcExpressionMacro.Attributes calculationAttributes
        = CalcExpressionMacro.getCalculationAttributes(document, Map.of(attribute, value));

    Assertions.assertEquals(value, calculationAttributes.getAttribute(attribute));
  }

  @ParameterizedTest
  @ValueSource(strings = {Macro.Key.AUTHOR, Macro.Key.LICENSE_TYPE})
  void getCalculationAttributesReturnsValueWithPriorityForMacroAttribute(String attribute) {
    String documentValue = UUID.randomUUID().toString();
    String macroValue = UUID.randomUUID().toString();

    Document document = Mockito.mock(Document.class);
    Mockito.when(document.getAttribute(attribute)).thenReturn(documentValue);
    CalcExpressionMacro.Attributes calculationAttributes
        = CalcExpressionMacro.getCalculationAttributes(document, Map.of(attribute, macroValue));

    Assertions.assertEquals(macroValue, calculationAttributes.getAttribute(attribute));
  }
}
