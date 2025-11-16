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
enum class UnitOfMeasure(val symbol: String, vararg val altSymbol: String) {
    ACRE("ac", "acre"),
    ACRE_FOOT("ac⋅ft", "ac ft", "ac⋅ft", "acft"),
    AMPERE("A"),
    ARC_MINUTE("′", "\'", "arc⋅min"), // minute of arc, same as symbol for feet (')
    ARC_SECOND("″", "\"", "arc⋅sec", "asec"), // same as symbol for inches (")
    ASTRONOMICAL_UNIT("au"),
    BIT("bit", "b"),
    BUSHEL("bsh"),
    BYTE("B"),
    CALORIE("Cal"),
    CARAT("ct"), // don't confuse the carat (ct) with the karat (K or kt)
    CELSIUS("°C", "C"),
    CENTILITER("cl"),
    CENTIMETER("cm"),
    CUBIC_CENTIMETER("cm3", "cm³"),
    CUBIC_FOOT("ft³", "cu ft", "cu. ft.", "cu⋅ft"),
    CUBIC_INCH("in³", "cu in", "cu. in.", "cu⋅in"),
    CUBIC_KILOMETER("km³", "km3"),
    CUBIC_METER("m³", "m3"),
    CUBIC_MILE("mi³", "cu mi", "cu. mi.", "cu⋅mi"),
    CUBIC_YARD("yd³", "cu yd", "cu. yd.", "cu⋅yd"),
    DECAMETER("dam"),
    CUP("cup"),
    DECILITER("dl"),
    DECIMETER("dm"),
    DEGREE("°", "deg"),
    FAHRENHEIT("°F", "F"),
    FATHOM("fth", "fm"),
    FLUID_OUNCE("fl⋅oz", "fl. oz."),
    FOODCALORIE("kcal"), // The term "food calorie" is a common name for the kilocalorie, which has the symbol kcal.
    FOOT("ft", "′", "'"),
    FURLONG("fur"),
    GALLON("gal"),
    GALLON_IMPERIAL("imp gal", "gal", "imp. gal.", "imp⋅gal"),
    GIGABIT("Gb"),
    GIGABYTE("GB"),
    GIGAHERTZ("GHz"),
    GIGAWATT("GW"),
    GRAM("g", "gm"),
    G_FORCE("Gs"),
    HECTARE("ha"),
    HECTOLITER("hL"),
    HECTOPASCAL("hPa"),
    HERTZ("Hz"),
    HORSEPOWER("hp", "HP"),
    INCH("in", "″", "\""),
    INCH_HG("inHg", "Hg"), //Inch of mercury
    JOULE("J"),
    KARAT("k", "kt"),
    KELVIN("K"),
    KILOBIT("kbit", "kb"),
    KILOBYTE("KiB", "kB"),
    KILOCALORIE("kCal"),
    KILOGRAM("kg"),
    KILOHERTZ("kHz"),
    KILOJOULE("kJ"),
    KILOMETER("km"),
    KILOMETER_PER_HOUR("km/h"),
    KILOWATT("kW"),
    KILOWATT_HOUR("kWh"),
    LIGHT_YEAR("ly"),
    LITER("L"),
    LITER_PER_KILOMETER("L/km"),
    LITER_PER_100KILOMETERS("L/1OOkm"),
    LUX("lx"),
    MEGABIT("Mbit", "Mb"),
    MEGABYTE("MiB", "MB"),
    MEGAHERTZ("MHz"),
    MEGALITER("Ml"),
    MEGAWATT("MW"),
    METER("m"),
    METER_PER_SECOND("m/s"),
    METER_PER_SECOND_SQUARED("m/s²", "m/s/s"),
    METRIC_TON("t", "ton"),
    MICROGRAM("μg", "mcg"),
    MICROMETER("μm"),
    MICROSECOND("μs"),
    MILE("mi"),
    MILE_PER_GALLON("mpg"),
    MILE_PER_HOUR("mph"),
    MILLIAMPERE("mA"),
    MILLIBAR("mb"),
    MILLIGRAM("mg"),
    MILLILITER("ml"),
    MILLIMETER("mm"),
    MILLIMETER_OF_MERCURY("mmHg", "mm Hg"),
    MILLISECOND("ms"),
    MILLIWATT("mW"),
    NANOMETER("nm"),
    NANOSECOND("ns"),
    NAUTICAL_MILE("NM", "nmi"),
    OHM("Ω", "ohm"),
    OUNCE("oz"),
    OUNCE_TROY("oz t", "ozt", "t oz", "oz⋅t"),
    PARSEC("pc"),
    PICOMETER("pm"),
    PINT("pt"),
    POUND("lb"),
    POUND_PER_SQUARE_INCH("psi", "PSI", "lbf/in²"),
    QUART("qt"),
    RADIAN("rad"),
    SQUARE_CENTIMETER("cm2", "cm²"),
    SQUARE_FOOT("ft²", "sq⋅ft", "sq. ft."),
    SQUARE_INCH("in²", "sq⋅in", "sq. in."),
    SQUARE_KILOMETER("km2", "km²"),
    SQUARE_METER("m2", "m²"),
    SQUARE_MILE("mi²", "sq⋅mi", "sq. mi."), //abbreviation sq mi or sq. mi. or mi2
    SQUARE_YARD("yd²", "sq⋅yd", "sq. yd."),
    STONE("st."),
    TABLESPOON("Tbsp", "tbsp"),
    TEASPOON("tsp"),
    TERABIT("Tbit", "Tb"),
    TERABYTE("TiB", "TB"),
    TON("ton"),
    VOLT("V"),
    WATT("W"),
    YARD("yd"),
    DAY("d"),
    HOUR("h"),
    MINUTE("min"),
    SECOND("s", "sec"),
    MONTH("mo", "month"), // english only, i18n should be reviewed
    WEEK("wk", "week"), // english only, i18n should be reviewed
    YEAR("a", "y", "yr", "year"), // english only, i18n should be reviewed
    YEAR_JULIAN("aj", "a"); // english only, i18n should be reviewed
}
