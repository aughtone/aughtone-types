# Third-Party Notices

This library embeds reference data whose terms are independent of the Apache License, Version 2.0 that covers this library's own code. See [NOTICE.md](NOTICE.md) for what is embedded, and [`docs/reference/README.md`](docs/reference/README.md) for provenance.

## ISO 4217 currency codes

The currency codes, names, numeric codes and minor-unit values compiled into `io.github.aughtone.types.financial.currencyResourceMap` are derived from the ISO 4217 currency code list published by the ISO 4217 Maintenance Agency.

Source: https://www.iso.org/iso-4217-currency-codes.html
Pinned snapshot: `docs/reference/list-one.xml`, published 2025-02-04.

The list is published as freely downloadable reference data. This library reproduces the code list to identify currencies and claims no rights in it. Consult the Maintenance Agency's own terms of use before redistributing the source file itself.

## Unicode CLDR

The localized display names compiled into `io.github.aughtone.types.locale.LocaleDisplayNameSupplement`, and into `LocaleDisplayNameTable` in the Linux artifact, are derived from the Unicode Common Locale Data Repository.

Source: https://cldr.unicode.org
Pinned source: `cldr-localenames-full` version 48.2.0, published on the npm registry.
Integrity: `sha512-4o0paYDz0UXhfhChAVFkmVl2CP0YTSEqE6WrC0fNafaSC5u5DAXphxH3SZ1Ujczarikr8DToKZfiok6EqWLdNQ==`
Licence text as shipped with that package: [`docs/reference/UNICODE-LICENSE-V3.txt`](docs/reference/UNICODE-LICENSE-V3.txt)

Names are composed from CLDR's own `languages`, `territories` and `scripts` data using CLDR's own `localeDisplayPattern`, rather than being transcribed, so the strings are CLDR's values arranged by CLDR's rule.

**A part of the data does not come from that release.** CLDR 48.2.0 cannot name 619 of the locale and display-language pairs this library ships. Those were taken instead from the CLDR data that platform implementations ship — `java.util.Locale` on a JDK 26 runtime, `NSLocale` on Apple, and `Intl.DisplayNames` in a Chromium browser — as measured on 2026-09-28. They remain Unicode CLDR data and carry the same licence, but they are **not reproducible from the pinned release**, because each platform ships its own CLDR version and, in places, its own additions. Of the names that ship, 16,590 come from the pinned release and 207 from platform implementations.

Six pairs can be named by neither, and fall back to the English name.

The Unicode License permits use, copying, modification, merging, publication, distribution and sale of the data, provided the copyright and permission notice appear with all copies or in associated documentation. This file, together with the licence text linked above, is that documentation.

## Unicode, Inc. — copyright and permission notice

Copyright © 2004-2026 Unicode, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy of data files and any associated documentation (the "Data Files") or software and any associated documentation (the "Software") to deal in the Data Files or Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, and/or sell copies of the Data Files or Software, and to permit persons to whom the Data Files or Software are furnished to do so, provided that either (a) this copyright and permission notice appear with all copies of the Data Files or Software, or (b) this copyright and permission notice appear in associated Documentation.

The full notice, including the warranty disclaimer and the terms on use of the Unicode name, is reproduced verbatim in [`docs/reference/UNICODE-LICENSE-V3.txt`](docs/reference/UNICODE-LICENSE-V3.txt) exactly as it ships with the pinned package.

## First-party English display names

The bundled English fallback table, `localeResourceMap`, is first-party curated data, not extracted from CLDR, and carries no third-party terms. The evidence for that — it uses forms CLDR has never published, and composes script into the display name in a way CLDR does not — is recorded in [`docs/reference/README.md`](docs/reference/README.md).
