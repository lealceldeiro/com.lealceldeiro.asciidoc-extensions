package com.lealceldeiro.asciidoc.extensions;

import com.lealceldeiro.asciidoc.extensions.chart.ChartMacro;
import java.util.logging.Logger;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

/**
 * Docs at
 * <a href="https://docs.asciidoctor.org/asciidoctorj/latest/extensions/extensions-introduction/">
 * AsciidocJ Extensions API
 * </a>
 */
public class ChartMacroExtensionRegistry implements ExtensionRegistry {
  private static final Logger LOGGER =
      Logger.getLogger(ChartMacroExtensionRegistry.class.getName());

  @Override
  public void register(Asciidoctor asciidoctor) {
    LOGGER.info("Registering ChartMacro");

    JavaExtensionRegistry javaExtensionRegistry = asciidoctor.javaExtensionRegistry();
    javaExtensionRegistry.block("chart", ChartMacro.class);
  }
}
