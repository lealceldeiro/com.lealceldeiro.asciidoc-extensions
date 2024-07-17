package com.lealceldeiro.asciidoc.extensions;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

import java.util.logging.Logger;

/**
 * Docs at
 * <a href="https://docs.asciidoctor.org/asciidoctorj/latest/extensions/extensions-introduction/">
 * AsciidocJ Extensions API
 * </a>
 */
public class CalcExpressionMacroExtensionRegistry implements ExtensionRegistry {
  private static final Logger LOGGER = Logger.getLogger(CalcExpressionMacroExtensionRegistry.class.getName());

  @Override
  public void register(Asciidoctor asciidoctor) {
    LOGGER.info("Registering " + CalcExpressionMacro.class.getName());

    JavaExtensionRegistry javaExtensionRegistry = asciidoctor.javaExtensionRegistry();
    javaExtensionRegistry.inlineMacro("calc_exp", CalcExpressionMacro.class);
  }
}
