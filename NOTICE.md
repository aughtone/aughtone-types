# NOTICE

Aughtone Types
Copyright 2025-2026 The Aught One Authors

This product is licensed under the Apache License, Version 2.0. A copy of that license is included in the [LICENSE](LICENSE) file at the root of this repository, and is also available at http://www.apache.org/licenses/LICENSE-2.0

## Embedded third-party data

This library compiles reference data into its published artifact. That data carries the terms of its own source, which are not the Apache License covering this library's code. Provenance for each dataset, and the pinned source snapshots, are recorded in [`docs/reference/`](docs/reference/).

- **Currency codes and names** — the values in `currencyResourceMap` are derived from the ISO 4217 currency code list published by the ISO 4217 Maintenance Agency. See [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md). Pinned snapshot: `docs/reference/list-one.xml`, published 2025-02-04.
- **Locale display names** — the English `displayName` values in `localeResourceMap` are first-party curated data, maintained under this project's own copyright above. They are **not** extracted from Unicode CLDR; see `docs/reference/README.md`.

Localized (non-English) display names are **not** embedded. They are resolved at runtime from the CLDR data each platform already ships, so no Unicode-licensed data is redistributed in this artifact — see [ADR-0003](docs/knowledge/decisions/localized-display-names-via-platform-cldr.md).

## No endorsement

The ISO 4217 Maintenance Agency does not endorse this library, and no endorsement is implied. Its name is used only to identify the origin of the embedded data.
