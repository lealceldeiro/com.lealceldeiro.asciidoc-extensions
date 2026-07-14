---
name: add-macro
description: Use when adding a new Asciidoctor macro (inline or block) to this extension library. Covers the processor + registry + dual SPI registration and the test/README conventions, so the macro actually loads and is verified end-to-end.
---

# Adding a new macro to asciidoc-extensions

A macro is a **triplet** — processor + registry + SPI registration — and it is not done
until it is registered in **both** SPI files, tested with a real render, and documented.
Work through the checklist in order.

## Checklist

1. **Processor class** — `src/main/java/com/lealceldeiro/asciidoc/extensions/<name>/<Name>Macro.java`.
   - Inline macro → extend `org.asciidoctor.extension.InlineMacroProcessor` and annotate
     with `@Name("<macro_name>")` (and `@PositionalAttributes(...)` if it takes positional
     args). A block macro (like `chart`) extends `BlockProcessor` instead.
   - Put **pure, unit-testable logic** in a `calculate(...)`-style method (or a helper
     class) so it can be tested without the Asciidoctor runtime. `process(...)` should just
     wire inputs, call that logic, and build the phrase/block node.
   - Reuse the shared building blocks rather than re-inventing them: attribute names go in
     `Macro.Key`, sentinel results in `InvalidValue`, rounding/sign→role helpers in `Util`,
     operators in `Operator`. Return an `InvalidValue` sentinel for bad input — **do not
     throw**.

2. **Registry class** — `src/main/java/com/lealceldeiro/asciidoc/extensions/<Name>MacroExtensionRegistry.java`
   implementing `org.asciidoctor.extension.spi.ExtensionRegistry`, registering the macro
   (e.g. `javaExtensionRegistry.inlineMacro("<macro_name>", <Name>Macro.class)` or
   `.block(...)` for a block macro). Model it on `CalcMacroExtensionRegistry`.

3. **⚠️ Register in BOTH SPI files** — this is the step that is silently forgotten; a macro
   that compiles and unit-tests fine will simply not load if either is missed. Add the fully
   qualified registry class name to **both**:
   - `src/main/resources/META-INF/services/org.asciidoctor.extension.spi.ExtensionRegistry`
   - `src/main/resources/META-INF/services/org.asciidoctor.jruby.extension.spi.ExtensionRegistry`

4. **Unit test** the pure logic — `src/test/java/.../<name>/<Name>MacroTest.java`, asserting
   on `calculate(...)` output including the relevant `InvalidValue` sentinels.

5. **Integration test** the real render — `<Name>MacroIntegrationTest.java` that calls
   `Asciidoctor.convert(doc, Options.builder().safe(SafeMode.UNSAFE).toFile(false).build())`
   and asserts on the produced HTML. This is what actually proves the SPI registration
   works. Model it on `ChartMacroIntegrationTest` / `CalcMacroIntegrationTest`. Note that
   surefire runs `*IntegrationTest` classes in the normal `test` phase.

6. **Run the suite:** `./mvnw test` (all green — no `-Dgpg.skip` needed for `test`).

7. **Document it in `README.md`** — add a `## \`<macro_name>\`` section following the
   existing macros' structure (usage examples, attributes, invalid-value behavior), and
   note the version it was introduced in.

## Notes

- If the macro evaluates user-supplied math via mXparser (like `calc_exp`), it also needs
  the document `author` (≥ 5 chars) and `calc_exp_license_type` attributes, or it returns
  `NaA` / `NaVA` / `NaL`. See `README.md` and `CLAUDE.md`.
- Adding a macro is a functional change → it warrants a version bump and release (see
  `CONTRIBUTING.md`), unlike config-only changes.
