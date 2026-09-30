package io.github.aughtone.types.units

/**
 * Represents a comprehensive collection of measurement units, covering various systems including metric (SI),
 * imperial, US customary, and digital information. Each unit is defined with a primary symbol and optional
 * alternative symbols for flexibility in parsing and display.
 *
 * The enum provides a standardized way to work with different units of measure, from length and mass to
 * time and digital storage.
 *
 * ### SI Units
 * This enum includes many base and derived SI units. For a detailed reference on SI units, see the
 * National Institute of Standards and Technology (NIST) guide:
 * @see <a href="https://www.nist.gov/pml/owm/metric-si/si-units">NIST SI Units</a>
 *
 * ### Digital Information Units
 * [Bit] and [Byte] and their multiples are **named** on the binary (JEDEC) reading, where a kilobyte
 * is 1024 bytes. Each family is regular:
 * - A prefixed **byte** leads with the unambiguous IEC symbol and keeps the decimal spelling as an
 *   alternative: [Kilobyte] is `KiB`/`kB`, [Megabyte] `MiB`/`MB`, [Gigabyte] `GiB`/`GB`, [Terabyte]
 *   `TiB`/`TB`.
 * - A prefixed **bit** leads with the spelled-out form and keeps the short form as an alternative:
 *   [Kilobit] is `kbit`/`kb`, [Megabit] `Mbit`/`Mb`, [Gigabit] `Gbit`/`Gb`, [Terabit] `Tbit`/`Tb`.
 *
 * Symbol lookup is **case-sensitive**, which is the only reason `GB` and `Gb` can name different
 * units. Do not lowercase a symbol before calling [findAll].
 *
 * **This enum names units; it does not convert between them.** An entry carries its [symbol] and any
 * [altSymbols], and nothing here multiplies by 1024 or by 1000 — the binary reading is what the names
 * mean, not arithmetic the library performs.
 */
enum class UnitOfMeasure(val symbol: String, vararg val altSymbols: String) {
    Acre("ac", "acre"),
    AcreFoot("ac⋅ft", "ac ft", "acft"),
    Ampere("A"),
    ArcMinute("′", "'", "arc⋅min"), // minute of arc, same as symbol for feet (')
    ArcSecond("″", "\"", "arc⋅sec", "asec"), // same as symbol for inches (")
    AstronomicalUnit("au"),
    Bit("bit", "b"),
    Bushel("bsh"),
    Byte("B"),
    Calorie("cal", "Cal"),
    Carat("ct"), // don't confuse the carat (ct) with the karat (K or kt)
    Celsius("°C", "C"),
    Centiliter("cl"),
    Centimeter("cm"),
    CubicCentimeter("cm3", "cm³"),
    CubicFoot("ft³", "cu ft", "cu. ft.", "cu⋅ft"),
    CubicInch("in³", "cu in", "cu. in.", "cu⋅in"),
    CubicKilometer("km³", "km3"),
    CubicMeter("m³", "m3"),
    CubicMile("mi³", "cu mi", "cu. mi.", "cu⋅mi"),
    CubicYard("yd³", "cu yd", "cu. yd.", "cu⋅yd"),
    Decameter("dam"),
    Cup("cup"),
    Deciliter("dl"),
    Decimeter("dm"),
    Degree("°", "deg"),
    Fahrenheit("°F", "F"),
    Fathom("fth", "fm"),
    FluidOunce("fl⋅oz", "fl. oz."),
    Foot("ft", "′", "'"),
    Furlong("fur"),
    Gallon("US gal", "gal", "gal (US)", "US gallon"),
    GallonImperial("imp gal", "gal", "imperial gal", "UK gal", "imp. gal.", "imp⋅gal"),
    Gigabit("Gbit", "Gb"),
    Gigabyte("GiB", "GB"),
    Gigahertz("GHz"),
    Gigawatt("GW"),
    Gram("g", "gm"),
    GForce("Gs"),
    Hectare("ha"),
    Hectoliter("hL"),
    Hectopascal("hPa"),
    Hertz("Hz"),
    Horsepower("hp", "HP"),
    Inch("in", "″", "\""),
    InchHg("inHg", "Hg"), //Inch of mercury
    Joule("J"),
    Karat("k", "kt"),
    Kelvin("K"),
    Kilobit("kbit", "kb"),
    Kilobyte("KiB", "kB"),
    Kilocalorie("kcal", "kCal"),
    Kilogram("kg"),
    Kilohertz("kHz"),
    Kilojoule("kJ"),
    Kilometer("km"),
    KilometerPerHour("km/h"),
    Kilowatt("kW"),
    KilowattHour("kWh"),
    LightYear("ly"),
    Liter("L"),
    LiterPerKilometer("L/km"),
    LiterPer100Kilometers("L/100km"),
    Lux("lx"),
    Megabit("Mbit", "Mb"),
    Megabyte("MiB", "MB"),
    Megahertz("MHz"),
    Megaliter("Ml"),
    Megawatt("MW"),
    Meter("m"),
    MeterPerSecond("m/s"),
    MeterPerSecondSquared("m/s²", "m/s/s"),
    MetricTon("t", "ton"),
    Microgram("μg", "mcg"),
    Micrometer("μm"),
    Microsecond("μs"),
    Mile("mi"),
    MilePerGallon("mpg"),
    MilePerHour("mph"),
    Milliampere("mA"),
    Millibar("mb"),
    Milligram("mg"),
    Milliliter("ml"),
    Millimeter("mm"),
    MillimeterOfMercury("mmHg", "mm Hg"),
    Millisecond("ms"),
    Milliwatt("mW"),
    Nanometer("nm"),
    Nanosecond("ns"),
    NauticalMile("NM", "nmi"),
    Ohm("Ω", "ohm"),
    Ounce("oz"),
    OunceTroy("oz t", "ozt", "t oz", "oz⋅t"),
    Parsec("pc"),
    Picometer("pm"),
    Pint("pt"),
    Pound("lb"),
    PoundPerSquareInch("psi", "PSI", "lbf/in²"),
    Quart("qt"),
    Radian("rad"),
    SquareCentimeter("cm2", "cm²"),
    SquareFoot("ft²", "sq⋅ft", "sq. ft."),
    SquareInch("in²", "sq⋅in", "sq. in."),
    SquareKilometer("km2", "km²"),
    SquareMeter("m2", "m²"),
    SquareMile("mi²", "sq⋅mi", "sq. mi."), //abbreviation sq mi or sq. mi. or mi2
    SquareYard("yd²", "sq⋅yd", "sq. yd."),
    Stone("st."),
    Tablespoon("Tbsp", "tbsp"),
    Teaspoon("tsp"),
    Terabit("Tbit", "Tb"),
    Terabyte("TiB", "TB"),
    Ton("ton"),
    Volt("V"),
    Watt("W"),
    Yard("yd"),
    Day("d"),
    Hour("h"),
    Minute("min"),
    Second("s", "sec"),
    Month("mo", "month"), // english only, i18n should be reviewed
    Week("wk", "week"), // english only, i18n should be reviewed
    Year("a", "y", "yr", "year"), // english only, i18n should be reviewed
    YearJulian("aj", "a", "a_j"); // english only, i18n should be reviewed

    companion object {
        /**
         * Finds the first `UnitOfMeasure` enum constant that matches the given symbol.
         *
         * An exact match on a primary `symbol` wins outright. Failing that, the alternate symbols
         * are searched — and if more than one unit claims the symbol, this **refuses** and returns
         * `null` rather than picking one by declaration order.
         *
         * That refusal is deliberate. `"gal"` is claimed by both [Gallon] and [GallonImperial],
         * which differ by about 20 percent, and each market qualifies the other one: a reader in
         * the United States writes `"imp gal"`, a reader in Canada or the United Kingdom writes
         * `"US gal"`. Returning either for a bare `"gal"` would be a confident wrong answer.
         *
         * Use [findAll] to discover the candidates and choose. The search is case-sensitive.
         *
         * @param symbol The string symbol to search for (e.g., "kg", "m", "ft").
         * @return The matching `UnitOfMeasure`, or `null` if nothing matches **or** if the symbol is
         *         ambiguous. Call [findAll] to tell those two cases apart: it returns an empty list
         *         for an unknown symbol and more than one entry for an ambiguous one.
         */
        fun findFirst(symbol: String?): UnitOfMeasure? {
            if (symbol == null) return null
            entries.find { it.symbol == symbol }?.let { return it }
            val matches = entries.filter { symbol in it.altSymbols }
            return matches.singleOrNull()
        }

        /**
         * Finds all `UnitOfMeasure` enum constants that match the given symbol.
         *
         * This method builds a list of all matches. It prioritizes entries where the
         * primary `symbol` matches, placing them at the beginning of the list, followed
         * by entries that match in their `altSymbols`. Duplicates are removed.
         *
         * @param symbol The string symbol to search for (e.g., "kg", "m", "ft").
         * @return A list of matching `UnitOfMeasure` constants, which may be empty.
         */
        fun findAll(symbol: String): List<UnitOfMeasure> {
            val primaryMatches = entries.filter { it.symbol == symbol }
            val altMatches = entries.filter { symbol in it.altSymbols }
            return (primaryMatches + altMatches).distinct()
        }
    }
}
