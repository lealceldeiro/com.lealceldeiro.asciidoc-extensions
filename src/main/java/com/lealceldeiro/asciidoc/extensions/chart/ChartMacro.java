package com.lealceldeiro.asciidoc.extensions.chart;

import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLogger;
import com.lealceldeiro.asciidoc.extensions.calclogger.ExtensionLoggerFactory;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.ContentModel;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.Contexts;
import org.asciidoctor.extension.Name;
import org.asciidoctor.extension.Reader;

/**
 * Block macro {@code [chart,line]} rendering a line chart as an embedded SVG image.
 *
 * <p>Docs at
 * <a href="https://docs.asciidoctor.org/asciidoctorj/latest/extensions/block-processor/">
 * Block Processor</a>.
 */
@Name("chart")
@Contexts({Contexts.LISTING, Contexts.LITERAL, Contexts.OPEN})
@ContentModel(ContentModel.RAW)
public class ChartMacro extends BlockProcessor {
  private static final ExtensionLogger logger = ExtensionLoggerFactory.getInstance();

  @Override
  public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
    // Positional attribute "1" holds the block style itself (e.g. "chart"); the
    // chart type is the first positional parameter after the style, at "2".
    String type = String.valueOf(attributes.getOrDefault("2", "line"));
    if (!"line".equalsIgnoreCase(type)) {
      logger.log(this, "Unsupported chart type '" + type + "'; only 'line' is supported.");
      return null;
    }

    List<String> lines = reader.readLines();
    Map<String, Object> docAttributes = parent.getDocument().getAttributes();
    ParsedChart parsed = ChartBlockParser.parse(lines, docAttributes, attributes);

    if (parsed.xLabels().isEmpty()) {
      logger.log(this, "Chart block has no 'x:' labels line; nothing rendered.");
      return null;
    }

    String svg = LineChartSvg.render(parsed.xLabels(), parsed.series(), parsed.options());
    File out;
    try {
      out = File.createTempFile("adoc-chart-", ".svg");
      out.deleteOnExit();
      Files.writeString(out.toPath(), svg);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to write chart SVG", e);
    }

    Map<String, Object> imageAttrs = new HashMap<>();
    imageAttrs.put("target", out.getAbsolutePath());
    imageAttrs.put("alt", attributes.getOrDefault("title", "chart"));
    imageAttrs.put("width", String.valueOf(parsed.options().width()));
    imageAttrs.put("height", String.valueOf(parsed.options().height()));

    Block image = createBlock(parent, "image", new ArrayList<String>(), imageAttrs);
    Object title = attributes.get("title");
    if (title != null) {
      image.setTitle(String.valueOf(title));
    }
    return image;
  }
}
