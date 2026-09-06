# DESIGN.md — the token file format

An emerging convention from Google Labs for describing a design system to
agents, so they stop inferring intent from screenshots.
[google-labs-code/design.md](https://github.com/google-labs-code/design.md),
Apache-2.0, **alpha** — expect it to move.

One file at the **repo root**: YAML front matter carrying machine-readable
tokens, then markdown prose carrying the rationale.

## Front matter

```yaml
version: alpha              # optional
name: Acme                  # REQUIRED - the only required field
description: ...            # optional
colors:
  <token-name>: <Color>
typography:
  <token-name>: <Typography>
rounded:
  <scale-level>: <Dimension>
spacing:
  <scale-level>: <Dimension | number>
components:
  <component-name>:
    <token-name>: <string | token reference>
```

## Token types

| Type | Format |
|---|---|
| **Color** | any valid CSS color — `#RGB`, `#RRGGBB`, `#RRGGBBAA`, named, `rgb()`, `hsl()`, `oklch()`, `oklab()` |
| **Dimension** | a string with a unit suffix: `px`, `em`, `rem` |
| **Typography** | object: `fontFamily`, `fontSize`, `fontWeight`, `lineHeight` (Dimension or unitless), `letterSpacing`, `fontFeature`, `fontVariation` |
| **Component** | `backgroundColor`, `textColor`, `typography`, `rounded`, `padding`, `size`, `height`, `width` |

## References

`{path.to.token}` — e.g. `{colors.primary}`.

Most groups may only reference **primitive** values. **Only `components`
may hold composite references** such as `{typography.label-md}`. Getting
this wrong is the easiest way to write a file that looks right and is not.

## Markdown body

Canonical order, all `##` headings. Any section may be omitted; the order
matters when they are present.

1. Overview *(or "Brand & Style")*
2. Colors
3. Typography
4. Layout *(or "Layout & Spacing")*
5. Elevation & Depth *(or "Elevation")*
6. Shapes
7. Components
8. Do's and Don'ts

## Validation

- **A duplicate section heading rejects the file.** This is the only hard
  failure — watch for it when merging or appending.
- Unknown sections, colour names and spacing values are **accepted**, so a
  typo in a token name fails silently rather than loudly.
- Unknown *component* properties raise a **warning**.

Tooling: `@google/design.md` on npm provides a CLI and a programmatic API.

## Using it

Read it before designing. Draw values from it rather than inventing them,
and when generating code reference the tokens rather than hardcoding hexes
and pixel values. When a design introduces a new token, add it here in the
same pass — a token file that lags the code is worse than none, because it
is trusted.
