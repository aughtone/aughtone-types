# NOTICE

Aughtone Types
Copyright 2025-2026 The Aught One Authors

This product is licensed under the Apache License, Version 2.0. A copy of that license is included in the [LICENSE](LICENSE) file at the root of this repository, and is also available at http://www.apache.org/licenses/LICENSE-2.0

## Embedded third-party data

This library compiles reference data into its published artifact. That data carries the terms of its own source, which are not the Apache License covering this library's code. Provenance for each dataset, and the pinned source snapshots, are recorded in [`docs/reference/`](docs/reference/).

- **Currency codes and names** — the values in `currencyResourceMap` are derived from the ISO 4217 currency code list published by the ISO 4217 Maintenance Agency. See [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md). Pinned snapshot: `docs/reference/list-one.xml`, published 2025-02-04.
- **Locale display names, English** — the `displayName` values in `localeResourceMap` are first-party curated data, maintained under this project's own copyright above. They are **not** extracted from Unicode CLDR; see `docs/reference/README.md`.
- **Locale display names, localized** — the names compiled into `LocaleDisplayNameSupplement` and, for the Linux target only, `LocaleDisplayNameTable` are derived from **Unicode CLDR** and carry the Unicode License. See [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md). Pinned source: `cldr-localenames-full` version 48.2.0.

Most localized display names are still resolved at runtime from the CLDR data each platform already ships, and the embedded tables only supply what a platform cannot — see [ADR-0005](docs/knowledge/decisions/completeness-over-consistency-for-display-names.md). That distinction does not change the licensing position: the tables are compiled into the published artifact, so this library redistributes CLDR-derived data and reproduces the Unicode License accordingly.

## No endorsement

Neither the ISO 4217 Maintenance Agency nor Unicode, Inc. endorses this library, and no endorsement is implied. Their names are used only to identify the origin of the embedded data.
