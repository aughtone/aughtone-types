# ADR 0002: Geo Prefixing to Avoid Shadowing

## Status
Accepted

## Context
When implementing GeoJSON geometries (Point, Polygon, Feature, etc.), naming them strictly according to the RFC 7946 specification causes massive name shadowing issues across Android and multiplatform projects. Classes like `Point` and `Polygon` are highly ubiquitous (e.g., `android.graphics.Point`, `java.awt.Point`, `org.jetbrains.skia.Point`). This creates ongoing friction with IDE auto-imports and readability.

## Decision
We will prefix all GeoJSON-related geometry and feature types with `Geo` (e.g., `GeoPoint`, `GeoPolygon`, `GeoLineString`, `GeoFeature`). 

However, to maintain strict adherence to the RFC 7946 GeoJSON specification during JSON serialization and deserialization, we must explicitly preserve the `@SerialName("Point")` annotations to match the required standard `"type"` values.

Additionally, `GeoPoint` natively supports 3D coordinates (longitude, latitude, altitude) by leveraging the standard third coordinate element in its array.

## Consequences
- **Positive:** Ambiguity and import conflicts are avoided across all dependent projects using UI/Graphics packages.
- **Negative:** Class names slightly deviate from the strict RFC terminology in Kotlin source code.
- **Mitigation:** The serialized JSON remains 100% strictly compliant via `@SerialName`.
