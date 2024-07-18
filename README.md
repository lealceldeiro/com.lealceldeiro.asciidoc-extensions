# com.lealceldeiro.asciidoc-extensions

This project was born from my need to evaluate some simple expressions within Asciidoc files and
output their results in the final generated document.

It's intended to be used along with the
[Asciidoctor Maven Plugin](https://docs.asciidoctor.org/maven-tools/latest/).

## Adding it to your project

Add the dependency to your `pom.xml` file as follows:

```xml
<dependency>
    <groupId>com.lealceldeiro</groupId>
    <artifactId>asciidoc-extensions</artifactId>
    <version>${asciidoc-extensions.version}</version>
</dependency>
```

To check what's the latest version you can use, as well as for other build tools,
please check https://central.sonatype.com/artifact/com.lealceldeiro/asciidoc-extensions

That's it,
the macros will be registered automatically for you
once the dependency is in your project's classpath.

For more info on how it is automatically registered for you,
please check the [Maven Plugin Configuration section](https://docs.asciidoctor.org/maven-tools/latest/plugin/goals/process-asciidoc/#configuration)
and [AsciidoctorJ's Extension API](https://docs.asciidoctor.org/asciidoctorj/latest/extensions/register-extensions-automatically/).

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
// outputs 1, as it's equivalent to calc:sum[1]
calc:sum[1,"this is a text", mode="ignore_invalid"]

// outputs NaN, as "this is a text" is not a valid number
calc:sum[1,"this is a text"]
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

## `calc_exp`

### Important License Notice

This macro is a wrapper around https://mathparser.org/. This means the actual logic to
calculate whatever expression is passed to the macro is performed by https://mathparser.org/

Before continue reading, it's important that you understand that this software doesn’t grant
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

If there's an invalid `author` value a `NaA` is returned.
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

If there's an invalid value for `calc_exp_license_type`,
then a `NaL` is reported.
Example:


```asciidoc
// outputs NaL
calc_exp:[exp=3 ^ 2, author=Johnny, calc_exp_license_type=testing]
```

If there's a failure while trying to evaluate the expression
then there's an `NaE` returned.
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
