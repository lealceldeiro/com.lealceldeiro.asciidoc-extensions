# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repository is

A small Java library of **Asciidoctor extensions**, published to Maven Central as
`com.lealceldeiro:asciidoc-extensions`. It provides custom macros used when rendering
AsciiDoc (to PDF/HTML):

- `calc` — simple arithmetic: `calc:sum[...]`, `sub`, `multiply`, `divide`
- `calc_exp` — evaluates a math expression (backed by mXparser)
- `calc_date` — date arithmetic / formatting
- `chart` — a line-chart **block** macro that renders an inline SVG

Full macro usage and attributes are documented in `README.md`; the release process is in
`CONTRIBUTING.md`. Prefer linking to those files over duplicating them here.

## Build & test

Java 25 (`zulu`) + Maven 3.9.9 — see `.sdkmanrc` (`sdk env`). Use the wrapper `./mvnw`.

```shell
./mvnw test                     # unit + integration tests (surefire runs *Test AND *IntegrationTest)
./mvnw -Dgpg.skip=true install  # build & install to the local ~/.m2 repo
```

**GPG gotcha:** the `maven-gpg-plugin` `sign` goal is bound to the **`verify`** phase, so
`test` and `package` are clean locally, but `verify` / `install` / `deploy` try to sign and
fail without the signing key — pass `-Dgpg.skip=true` for local runs. CI imports the key
from secrets, so never bake `-Dgpg.skip` into the build itself.

No checkstyle/formatter plugin runs: style is governed by `.editorconfig` (2-space indent)
and analyzed by SonarCloud. Keep imports clean (no unused imports).

## Architecture: the macro pattern

Each macro is a **triplet**, and adding one touches all three parts plus tests:

1. A processor class (e.g. `calc/CalcMacro.java`) extending `InlineMacroProcessor`
   (or `BlockProcessor`, as `chart` does).
2. An `*ExtensionRegistry` class (e.g. `CalcMacroExtensionRegistry.java`) that registers
   the macro name with the AsciidoctorJ registry.
3. **Both** SPI service files under `src/main/resources/META-INF/services/` —
   `org.asciidoctor.extension.spi.ExtensionRegistry` **and**
   `org.asciidoctor.jruby.extension.spi.ExtensionRegistry` — must list the registry class.
   Missing either file means the macro silently fails to load. This is the easiest step to
   forget; the `/add-macro` skill walks the full checklist.

Shared building blocks (package root `com.lealceldeiro.asciidoc.extensions`): `Macro.Key` /
`Macro.Value` (attribute names), `Operator`, `Util` (rounding + sign→role helpers),
`InvalidValue` (sentinel results), `Calc` (interface), `calclogger/` (logging).

## Conventions & gotchas

- **Macros return sentinel strings; they do not throw.** Invalid input yields an
  `InvalidValue` code — `NaN` (bad number), `NaO` (bad operation), `NaE` (bad expression),
  `NaL` (bad license), `NaA` / `NaVA` (missing / too-short author), `NaVM` (invalid math),
  `NaD` / `NaF` (bad date / format). Tests assert on these strings.
- **`calc_exp` needs a license.** mXparser requires license confirmation, so `calc_exp`
  returns `NaA` / `NaVA` without a valid document `author` (≥ 5 chars) and `NaL` without
  `calc_exp_license_type` (`commercial` | `non_commercial`).
- **Results are `BigDecimal` at scale 2**, rounded `HALF_EVEN` by default (override via the
  `rounding_mode` attribute).
- **Testing convention:** pure logic is unit-tested against `calculate(...)`; end-to-end
  rendering is covered by `*IntegrationTest` classes that call `Asciidoctor.convert(...)`
  and assert on the produced HTML.

## Releasing

See `CONTRIBUTING.md`. In short: bump `<version>` on a chore branch → PR → merge → tag
`release/x.y.z` on `main` → `git push --tags` (the `maven-publish` workflow deploys to
Central). Config-only changes with no functional impact don't need a version bump/release.
