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
 * The `BIT` and `BYTE` related units (e.g., `KILOBYTE`, `MEGABYTE`) are based on their binary (JEDEC)
 * definitions, where 1 kilobyte = 1024 bytes. To reduce ambiguity, the primary symbols for these
 * units use the IEC standard prefixes (Ki, Mi, Ti).
 *
 * Common decimal-based symbols (`kB`, `MB`, etc.) are included as alternative symbols, but be aware of
 * their potential ambiguity (e.g., `MB` can mean 10^6 or 2^20 bytes). This enum consistently uses the
 * binary (powers-of-1024) interpretation.
 * - `KILOBYTE` uses primary symbol `KiB` (kibibyte) and alt symbol `kB`.
 * - `MEGABYTE` uses primary symbol `MiB` (mebibyte) and alt symbol `MB`.
 * - `TERABYTE` uses primary symbol `TiB` (tebibyte) and alt symbol `TB`.
 *
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
    Calorie("Cal"),
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
    FoodCalorie("kcal"), // The term "food calorie" is a common name for the kilocalorie, which has the symbol kcal.
    Foot("ft", "′", "'"),
    Furlong("fur"),
    Gallon("gal"),
    GallonImperial("imp gal", "gal", "imp. gal.", "imp⋅gal"),
    Gigabit("Gb"),
    Gigabyte("GB"),
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
    Kilocalorie("kCal"),
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
    YearJulian("aj", "a"); // english only, i18n should be reviewed

    companion object {
        /**
         * Finds the first `UnitOfMeasure` enum constant that matches the given symbol.
         *
         * This method prioritizes an exact match on the primary `symbol` before searching
         * through the `altSymbols`. The search is case-sensitive.
         *
         * @param symbol The string symbol to search for (e.g., "kg", "m", "ft").
         * @return The matching `UnitOfMeasure` constant, or `null` if no match is found.
         */
        fun findFirst(symbol: String?): UnitOfMeasure? {
            if (symbol == null) return null
            return entries.find { it.symbol == symbol } ?: entries.find { symbol in it.altSymbols }
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
