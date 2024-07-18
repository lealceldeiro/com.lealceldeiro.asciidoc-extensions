# com.lealceldeiro.asciidoc-extensions

This project was born from my need to evaluate some simple expressions within Asciidoc files and
output their results in the final generated document.

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

## Limitations

For the time being,
the results are being returned as `BigDecimal`s with scale 2 and `RoundingMode.CEILING`.
I plan to provide ways to customize that, but I haven't had the time yet.
PRs are welcome.
