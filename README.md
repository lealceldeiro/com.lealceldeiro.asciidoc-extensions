# com.lealceldeiro.asciidoc-extensions

[![License: MIT](https://img.shields.io/badge/License-MIT-blue)](https://opensource.org/licenses/MIT)
[![maven-central](https://img.shields.io/maven-central/v/com.lealceldeiro/asciidoc-extensions?style=flat)](https://central.sonatype.com/artifact/com.lealceldeiro/asciidoc-extensions)
[![Maven Build](https://github.com/lealceldeiro/com.lealceldeiro.asciidoc-extensions/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/lealceldeiro/com.lealceldeiro.asciidoc-extensions/actions/workflows/maven-publish.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=lealceldeiro-com_com-lealceldeiro-asciidoc-extensions&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=lealceldeiro-com_com-lealceldeiro-asciidoc-extensions)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=lealceldeiro-com_com-lealceldeiro-asciidoc-extensions&metric=coverage)](https://sonarcloud.io/summary/new_code?id=lealceldeiro-com_com-lealceldeiro-asciidoc-extensions)

[//]: # ()
[//]: # ([![CodeQL]&#40;https://github.com/lealceldeiro/com.lealceldeiro.asciidoc-extensions/actions/workflows/codeql-analysis.yml/badge.svg&#41;]&#40;https://github.com/lealceldeiro/com.lealceldeiro.asciidoc-extensions/actions/workflows/codeql-analysis.yml&#41;)


This project was born from my need to evaluate some simple mathematical expressions
within _AsciiDoc_ files and
output their results in the final generated document.

It is intended to be used along with the
[Asciidoctor Maven Plugin](https://docs.asciidoctor.org/maven-tools/latest/),
the [IntelliJ AsciiDoc Plugin](https://intellij-asciidoc-plugin.ahus1.de/docs/users-guide/index.html),
or a similar tool that allows importing this dependency as an Asciidoctor Java extension.

## Compatibility

- `com.lealceldeiro:asciidoc-extensions:0.*.*` is compatible with `org.asciidoctor:asciidoctorj:2.5.13`, Java 11
- `com.lealceldeiro:asciidoc-extensions:1.*.*` is compatible with `org.asciidoctor:asciidoctorj:3.0.0`, Java 11
- `com.lealceldeiro:asciidoc-extensions:2.*.*` is compatible with `org.asciidoctor:asciidoctorj:3.0.0`, Java 21

## Adding it to your project

### Asciidoctor Maven Plugin

Add the dependency to your `pom.xml` file as follows:

```xml
<dependency>
    <groupId>com.lealceldeiro</groupId>
    <artifactId>asciidoc-extensions</artifactId>
    <version>${asciidoc-extensions.version}</version>
</dependency>
```

To check what the latest version you can use is, as well as for other build tools,
please check https://central.sonatype.com/artifact/com.lealceldeiro/asciidoc-extensions.

That is it,
the macros will be registered automatically for you
once the dependency is in your project's classpath.

For more info on how it is automatically registered for you,
please check the [Maven Plugin Configuration section](https://docs.asciidoctor.org/maven-tools/latest/plugin/goals/process-asciidoc/#configuration)
and [AsciidoctorJ's Extension API](https://docs.asciidoctor.org/asciidoctorj/latest/extensions/register-extensions-automatically/).

### IntelliJ AsciiDoc Plugin

Starting from version `2.1.0`, this extension can be used along with the
[IntelliJ AsciiDoc Plugin](https://intellij-asciidoc-plugin.ahus1.de/docs/users-guide/index.html).
To know how this type of extensions can be used along with the plugin, please refer to the plugin
[official documentation](https://intellij-asciidoc-plugin.ahus1.de/docs/users-guide/features/advanced/asciidoctor-extensions.html).
Make sure the [dependency](https://central.sonatype.com/artifact/com.lealceldeiro/asciidoc-extensions/versions)
(jar file) you use to be added to your project classpath is the one ending with
`jar-with-dependencies.jar`.
For example `asciidoc-extensions-2.1.0-jar-with-dependencies.jar` (replace `2.1.0` with the
latest version available in Maven).

It is important to notice that the jar-with-dependencies that is used for this purpose
contains a dependency to https://central.sonatype.com/artifact/org.mariuszgromada.math/MathParser.org-mXparser.
Make sure you read the section for the `calc_exp` macro shown below,
for more information about this.

## The macros
So far it includes three inline macros:

- `calc`
- `cal_date`
- `calc_exp`

## `calc`

Allows making simple calculation within the asciidoc document. Example:

```asciidoc
// outputs 3: 1 + 2
calc:sum[1, 2]

// outputs 1: 5 - 3 - 1
calc:sub[5, 3, 1]

// outputs 12: 4 * 3
calc:multiply[4, 3]

// outputs 5: 10 / 2
calc:divide[10, 2]
```

It allows for attributes substitution as well. Example:

```asciidoc
= Title

:price: 50
:units: 3

// outputs 150
calc:multiply[{price}, {units}]
```

### Invalid arguments

If the `calc` macro isn't provided with a valid operation, that's it,
one of `sum`, `sub`, `multiply`, or `divide`, then `NaO` is returned as a result.

If any of the values to be used in the operation, those within square brackets (`[]`), is not valid,
then `NaN` is returned as a result.
For instance, this call would return `NaN`: `calc:sum["a_text", 2]`.

There's an exception to this rule: if the argument `mode` is provided with `ignore_invalid`, then,
invalid "number" arguments are ignored,
and the calculation is performed as if it was not provided at all.
For example:

```asciidoc
// these two options, both, output 1, as it's equivalent to calc:sum[1]
calc:sum[1,"this is a text", mode="ignore_invalid"]
calc:sum[ignore_invalid, 1,"this is a text"]

// outputs NaN, as "this is a text" is not a valid number
calc:sum[1,"this is a text"]

// outputs NaN, as "ignore_invalid" was neither provided as a named argument nor as an unnamed one in the first position 
calc:sum[1, 2, ignore_invalid]
```

If there's any exception while doing the calculation because of an arithmetic rule,
then `NaVM` is returned.
For example:

```asciidoc
// outputs `NaVM`, as division by zero is not mathematically possible
calc:divide[4, 0]

// outputs `0`, as dividing zero by another number different from zero is mathematically possible
calc:divide[0, 4]
```

## `calc_date`

Similarly, the `calc_date` performs two simple operations on dates: addition and subtraction.
Examples:

```asciidoc
// outputs 2024-01-03: 1st January 2024 + 2 days
calc_date:sum[2024-01-01, 2d]

// outputs 2024-01-06: 1st January 2024 + 5 days
calc_date:sum[2024-01-01, 5]

// outputs 2023-12-01: 1st January 2024 - 1 month
calc_date:sub[2024-01-01, 1m]
```

As you can see from the previous examples, you can indicate the amount to add or subtract by using
`d` (default) for days, `m` for months, and `y` for years.

Optionally, you can modify the format of the resulting date.
Any valid expression for the
Java [DateTimeFormatter](https://docs.oracle.com/en%2Fjava%2Fjavase%2F11%2Fdocs%2Fapi%2F%2F/java.base/java/time/format/DateTimeFormatter.html) will work.
For example:

```asciidoc
// outputs Jan 1, 2023
calc_date:sub[2024-01-01, 1y, format="MMM d, yyyy"]
```

Additionally, you can specify the _source_ and _target_ zone ids for the date being handled.
This is useful when you want the output date rendered in a different timezone than the one
in which the original date was provided (i.e: the zone for the "machine" where the document is
being rendered).

If no values are provided for the source and target zone ids,
or they're invalid zone id values, then the
[system default zone id](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/ZoneId.html#systemDefault())
is used.

For example, assuming the document is being rendered at `12:24` in a machine running in the
`America/Adak` timezone and the target timezone is `Pacific/Tarawa`, then the following call
would return `2025-08-31` as the result::

```asciidoc
// outputs 2025-08-31
calc_date:sub[2025-08-30, 0, to_zone_id="Pacific/Tarawa"]
```

Let's see other examples, all of them assuming the document is generated at
(UTC) `2025-08-30T21:24:00.000[UTC]`.
For simplicity, the amount to be subtracted is always `0`.:

```asciidoc
// outputs 2025-08-31
calc_date:sub[2025-08-30, 0, from_zone_id="America/Adak", to_zone_id="Pacific/Tarawa"]
```

```asciidoc
// outputs 2025-08-29
// in Athens, it's 30th at 00:24:00, so when converted to UTC (21:24:00),
// then it's "moved back" to the previous day
calc_date:sub[2025-08-30, 0, from_zone_id="Europe/Athens", to_zone_id="UTC"]
```

### Invalid arguments

If the `calc_date` macro isn't provided with a valid operation, that's it,
one of `sum`, or `sub`, then `NaO` is returned as a result.

For instance, this call would return `NaO`: `calc_date:multiply[2024-01-01, 2]`.

If any of the values to be used in the operation, those within square brackets (`[]`), is not valid,
then an appropriate value is returned.

For example,
if an invalid date is provided, then `NaD` is returned as a result.
For instance, this call would return `NaD`: `calc_date:sum["a_text", 2]`.

Also, dates without one of the valid standard formats as described in the
Java [DateTimeFormatter](https://docs.oracle.com/en%2Fjava%2Fjavase%2F11%2Fdocs%2Fapi%2F%2F/java.base/java/time/format/DateTimeFormatter.html) class will be considered as invalid.
For example, this call would return `NaD`: `calc_date:sum[2024 01 01, 2]`.
In the future, I'll add support to specify the input format pattern.
PRs are welcome!

Additionally, if the output format is specified, but it's invalid.
Then `NaF` is returned.
For example, this call would return it: `calc_date:sum[2024-01-01, 2, format="j0"]`

Similarly, if the amount to be used in the operation is not a valid number
then the returned value is `NaN`.
For example:

```asciidoc
// outputs NaN: 'f' is not a number, although 'y' is interpreted as years to be added
test: calc_date:sum[2024-01-01, fy]
```

This macro also supports the `mode="ignore_invalid"` argument described previously.
When it's used, any invalid value will be replaced as follows:

- `date` is replaced by `LocalDate#now()`
- `amount` is replaced by `0`
- `format` is replaced by `DateTimeFormatter.ISO_DATE`

For example:

```asciidoc
// supposing LocalDate#now() returns 2025-01-01, then this returns 2025-01-02 as it replaces the invalid date
calc_date:sum["1st of Jan 2024", 1, mode=ignore_invalid]
```

```asciidoc
// this returns 2024-01-01 as `0` replaces the invalid value to add to the date
calc_date:sum[2024-01-01, xyz, mode=ignore_invalid]
```

```asciidoc
// these two options return 2024-01-03 as the default format replaces the invalid one
calc_date:sum[2024-01-01, 2, format="j0", mode=ignore_invalid]
calc_date:sum[2024-01-01, 2, format="j0", ignore_invalid]
```

## `calc_exp`

### Important License Notice

This macro is a wrapper around https://mathparser.org/. This means the actual logic to
calculate whatever expression is passed to the macro is performed by https://mathparser.org/

Before continue reading, it is important that you understand that this software doesn’t grant
you any type of license for use of https://mathparser.org/.

While this software is [licensed under MIT](./LICENSE),
https://mathparser.org/ has its own License Agreement,
Terms and Conditions, etc., to which you must adhere.

I kindly ask you to take 5 minutes
and read [their license](https://mathparser.org/mxparser-license/) before using this macro.

In short, if you're obliged to purchase a license from https://mathparser.org/ because of the
final use you'll give to their, or this, software; then you must do so because (as stated before)
the use of this library doesn’t grant you any rights over https://mathparser.org/.

Likewise, if you import this dependency in you project, but don't plan to use the `calc_exp` macro,
you don't have to worry about any of this.

### Usage

Getting the legal bits out of our way, let's see how it can be used.

For this macro to work for you, the document in which it's used must contain a valid author
(Asciidoc [`:author:`](https://docs.asciidoctor.org/asciidoc/latest/document/reference-author-attributes/#reference-author) attribute).
Optionally, the `author` macro attribute can be provided instead of using the document attribute.
In both cases, it must be a value with length greater than 5 characters.
See some examples below.

If there isn't such a valid value for this attribute you'll get `NaA` or `NaVA` as a result.

There's a second mandatory value that you must provide: `calc_exp_license_type`, which can be
either `commercial` or `non_commercial`.
This value can be provided directly as an attribute for the macro, or at the document level.

- `non_commercial`: indicates that you haven’t purchased any license for commercial use,
  from https://mathparser.org/ and that you plan to use it for non-commercial purposes.
- `commercial`: indicates that you’ve purchased a license for commercial use,
from https://mathparser.org/

If there isn't any valid value provided for this attribute you'll get a `NaL` as a result.

These two values are used to acknowledge that you comply with the license agreement from
https://mathparser.org/.
See more at https://mathparser.org/mxparser-tutorial/confirming-non-commercial-commercial-use/

Example, setting the author and license type at the document level:

```asciidoc
= My Document
Asiel Leal_Celdeiro

:calc_exp_license_type: non_commercial

// outputs 1.00
calc_exp:[1 ^ 2]
```

Example, setting the author and license type at the macro level:

```asciidoc
= My Document

// outputs 9.00
calc_exp:[exp=3 ^ 2, author=Johnny, calc_exp_license_type=commercial]
```

### Invalid values

If there is an invalid `author` value a `NaA` is returned.
A special case is when
the value is shorter than `5` characters, where you get a `NaVA` (this
special case is due to the underlying library)

Example:

```asciidoc
// outputs NaA: if the document-level author is null or invalid
calc_exp:[exp=3 ^ 2, calc_exp_license_type=non_commercial]
```

```asciidoc
// outputs NaVA
calc_exp:[exp=3 ^ 2, author=John, calc_exp_license_type=non_commercial]
```

If there is an invalid value for `calc_exp_license_type`,
then a `NaL` is reported.
Example:


```asciidoc
// outputs NaL
calc_exp:[exp=3 ^ 2, author=Johnny, calc_exp_license_type=testing]
```

If there is a failure while trying to evaluate the expression
then there is an `NaE` returned.
Example:

```asciidoc
// outputs NaE
calc_exp:[exp=3 ^, author=Johnny, calc_exp_license_type=non_commercial]
```

## Limitations

For the time being,
the results are being returned as `BigDecimal`s with scale 2 and `RoundingMode.CEILING`.
I plan to provide ways to customize that, but I haven't had the time yet.
PRs are welcome.

## Contributing to this project

You can contribute to this project!
Read [our contribution guidelines](CONTRIBUTING.md).
