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


## `calc_exp`

## Limitations

For the time being,
the results are being returned as `BigDecimal`s with scale 2 and `RoundingMode.CEILING`.
I plan to provide ways to customize that, but I haven't had the time yet.
PRs are welcome.
