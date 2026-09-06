# Third-Party Notices

This library embeds reference data whose terms are independent of the Apache License, Version 2.0 that covers this library's own code. See [NOTICE.md](NOTICE.md) for what is embedded, and [`docs/reference/README.md`](docs/reference/README.md) for provenance.

## ISO 4217 currency codes

The currency codes, names, numeric codes and minor-unit values compiled into `io.github.aughtone.types.financial.currencyResourceMap` are derived from the ISO 4217 currency code list published by the ISO 4217 Maintenance Agency.

Source: https://www.iso.org/iso-4217-currency-codes.html
Pinned snapshot: `docs/reference/list-one.xml`, published 2025-02-04.

The list is published as freely downloadable reference data. This library reproduces the code list to identify currencies and claims no rights in it. Consult the Maintenance Agency's own terms of use before redistributing the source file itself.

## Unicode CLDR — not redistributed

This library does **not** embed Unicode CLDR data, so the Unicode License is not reproduced here.

Localized display names are resolved at runtime from the CLDR data each platform already ships (`java.util.Locale` on JVM and Android, `NSLocale` on Apple, `Intl.DisplayNames` on JS and Wasm) rather than being bundled — see [ADR-0003](docs/knowledge/decisions/localized-display-names-via-platform-cldr.md). Calling a platform API is use, not redistribution.

The bundled English fallback table is first-party curated data, not extracted from CLDR, and carries no third-party terms. Its history is recorded in [`docs/reference/README.md`](docs/reference/README.md).
