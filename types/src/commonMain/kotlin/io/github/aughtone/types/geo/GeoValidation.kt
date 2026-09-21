package io.github.aughtone.types.geo

/**
 * Structural checks shared by the geometry types, from RFC 7946 §3.1.
 *
 * These are the rules the RFC requires a geometry to satisfy, and they run on construction *and* on
 * deserialization, because an `init` block runs on both. That is deliberate: a document violating
 * them is not GeoJSON, and accepting it would produce a value that cannot serialize correctly.
 *
 * Winding order is deliberately **not** checked here. RFC 7946 §3.1.6 makes the right-hand rule a
 * MUST for producers, and in the same section instructs parsers not to reject polygons that break it,
 * for backwards compatibility. Enforcing it in `init` would make deserialization strict against the
 * specification's explicit direction. It is enforced by [geoPolygon] instead, which is the
 * construction path rather than the parsing path.
 */
internal object GeoValidation {

    fun position(position: List<Double>, where: String) {
        require(position.size >= 2) {
            "$where: a position needs at least two elements (longitude, latitude), found ${position.size}."
        }
    }

    fun positions(positions: List<List<Double>>, where: String) {
        positions.forEachIndexed { i, p -> position(p, "$where position $i") }
    }

    fun lineString(positions: List<List<Double>>, where: String) {
        require(positions.size >= 2) {
            "$where: a LineString needs at least two positions, found ${positions.size}."
        }
        positions(positions, where)
    }

    fun linearRing(ring: List<List<Double>>, where: String) {
        require(ring.size >= 4) {
            "$where: a linear ring needs at least four positions, found ${ring.size}."
        }
        positions(ring, where)
        require(ring.first() == ring.last()) {
            "$where: a linear ring must be closed — the first position ${ring.first()} must equal the last ${ring.last()}."
        }
    }

    fun polygon(rings: List<List<List<Double>>>, where: String) {
        rings.forEachIndexed { i, ring ->
            linearRing(ring, "$where ring $i" + if (i == 0) " (exterior)" else " (hole)")
        }
    }
}

/** The direction a linear ring is wound. */
enum class Winding {
    /** Wound clockwise. RFC 7946 expects this of holes. */
    Clockwise,

    /** Wound counter-clockwise. RFC 7946 expects this of exterior rings. */
    CounterClockwise,

    /** The ring encloses no area, so it has no direction. */
    Degenerate,
}

/**
 * The winding of one of this polygon's rings, computed from the signed area of the ring — the
 * shoelace sum over its edges.
 *
 * @param ringIndex 0 for the exterior ring, 1 and above for holes.
 */
fun GeoPolygon.windingOf(ringIndex: Int = 0): Winding = windingOfRing(coordinates[ringIndex])

internal fun windingOfRing(ring: List<List<Double>>): Winding {
    var twiceArea = 0.0
    for (i in 0 until ring.size - 1) {
        val (x1, y1) = ring[i]
        val (x2, y2) = ring[i + 1]
        twiceArea += (x2 - x1) * (y2 + y1)
    }
    return when {
        twiceArea > 0.0 -> Winding.Clockwise
        twiceArea < 0.0 -> Winding.CounterClockwise
        else -> Winding.Degenerate
    }
}

/**
 * This polygon with every ring wound as RFC 7946 §3.1.6 requires: the exterior ring
 * counter-clockwise, holes clockwise. Rings already wound correctly are left untouched.
 *
 * This is the function to reach for after reading a foreign document. Parsing deliberately accepts
 * any winding, so a caller who needs conformant output normalizes explicitly.
 */
fun GeoPolygon.rewound(): GeoPolygon = copy(coordinates = rewindRings(coordinates))

internal fun rewindRings(rings: List<List<List<Double>>>): List<List<List<Double>>> =
    rings.mapIndexed { index, ring ->
        val wanted = if (index == 0) Winding.CounterClockwise else Winding.Clockwise
        if (windingOfRing(ring) == Winding.Degenerate || windingOfRing(ring) == wanted) ring
        else ring.reversed()
    }

/**
 * Builds a [GeoPolygon], rejecting rings that break the right-hand rule.
 *
 * This is the correct-by-default construction path. Use [geoPolygonRewinding] for data that arrives
 * from elsewhere with arbitrary winding.
 *
 * @throws IllegalArgumentException if a ring is structurally invalid, or is wound the wrong way.
 */
fun geoPolygon(
    coordinates: List<List<List<Double>>>,
    bbox: List<Double>? = null,
): GeoPolygon {
    val polygon = GeoPolygon(coordinates, bbox)
    coordinates.forEachIndexed { index, ring ->
        val wanted = if (index == 0) Winding.CounterClockwise else Winding.Clockwise
        val actual = windingOfRing(ring)
        require(actual == wanted || actual == Winding.Degenerate) {
            val role = if (index == 0) "exterior ring" else "hole"
            "Polygon ring $index ($role) is wound $actual but RFC 7946 requires $wanted. " +
                "Use geoPolygonRewinding(...) to accept and correct foreign data."
        }
    }
    return polygon
}

/**
 * Builds a [GeoPolygon] from coordinates of any winding, returning it correctly wound.
 *
 * Structural rules are still enforced; only the winding is forgiven.
 */
fun geoPolygonRewinding(
    coordinates: List<List<List<Double>>>,
    bbox: List<Double>? = null,
): GeoPolygon = GeoPolygon(rewindRings(coordinates), bbox)
