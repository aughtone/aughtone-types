package io.github.aughtone.types.financial

import io.github.aughtone.types.util.lazyMapOf

/**
 * Data Source: https://www.iso.org/iso-4217-currency-codes.html
 *
 * This map is meant to supplement curency information on platforms that don't normally contain it.
 *
 * A map containing currency data, keyed by the three-letter ISO 4217 currency code.
 * Each entry's value is a lambda that returns a [Currency] object, which
 * contains the currency's display name and numeric code.
 *
 * This map is lazily initialized using [lazyMapOf], meaning the values are
 * computed only when they are first accessed.
 *
 * The map does not contain any entries for special codes like precious metals, testing, or No currency involved.
 *
 * @see Currency
 * @see lazyMapOf
 */

internal val currencyResourceMap: Map<String, Currency> = mapOf(
    "AED" to lazy { Currency("AED", 784, 2, "UAE Dirham",  "د.إ") },
    "AFN" to lazy { Currency("AFN", 971, 2, "Afghani",   "؋") },
    "ALL" to lazy { Currency("ALL", 8, 2, "Lek",  "L") },
    "AMD" to lazy { Currency("AMD", 51, 2, "Armenian Dram",   "֏") },
    "ANG" to lazy { Currency("ANG", 532, 2, "Netherlands Antillean Guilder", "ƒ", obsolete = true, replacedBy = "XCG") },
    "AOA" to lazy { Currency("AOA", 973, 2, "Kwanza",  "Kz") },
    "ARS" to lazy { Currency("ARS", 32, 2, "Argentine Peso",  "$") },
    "AUD" to lazy { Currency("AUD", 36, 2, "Australian Dollar",  "$") },
    "AWG" to lazy { Currency("AWG", 533, 2, "Aruban Florin",  "ƒ") },
    "AZN" to lazy { Currency("AZN", 944, 2, "Azerbaijan Manat",  "₼") },
    "BAM" to lazy { Currency("BAM", 977, 2, "Convertible Mark",  "KM") },
    "BBD" to lazy { Currency("BBD", 52, 2, "Barbados Dollar",  "$") },
    "BDT" to lazy { Currency("BDT", 50, 2, "Taka",  "৳") },
    "BGN" to lazy { Currency("BGN", 975, 2, "Bulgarian Lev",  "лв") },
    "BHD" to lazy { Currency("BHD", 48, 3, "Bahraini Dinar",  ".د.ب") },
    "BIF" to lazy { Currency("BIF", 108, 0, "Burundi Franc",  "FBu") },
    "BMD" to lazy { Currency("BMD", 60, 2, "Bermudian Dollar",  "$") },
    "BND" to lazy { Currency("BND", 96, 2, "Brunei Dollar",  "$") },
    "BOB" to lazy { Currency("BOB", 68, 2, "Boliviano",  "\$b") },
    "BOV" to lazy { Currency("BOV", 984, 2, "Mvdol",  "BOV") },
    "BRL" to lazy { Currency("BRL", 986, 2, "Brazilian Real",  "R$") },
    "BSD" to lazy { Currency("BSD", 44, 2, "Bahamian Dollar",  "$") },
    "BTN" to lazy { Currency("BTN", 64, 2, "Ngultrum",  "Nu.") },
    "BWP" to lazy { Currency("BWP", 72, 2, "Pula",  "P") },
    "BYN" to lazy { Currency("BYN", 933, 2, "Belarusian Ruble",  "Br") },
    "BZD" to lazy { Currency("BZD", 84, 2, "Belize Dollar",  "BZ$") },
    "CAD" to lazy { Currency("CAD", 124, 2, "Canadian Dollar",  "$") },
    "CDF" to lazy { Currency("CDF", 976, 2, "Congolese Franc",  "FC") },
    "CHE" to lazy { Currency("CHE", 947, 2, "WIR Euro",  "CHE") },
    "CHF" to lazy { Currency("CHF", 756, 2, "Swiss Franc",  "CHF") },
    "CHW" to lazy { Currency("CHW", 948, 2, "WIR Franc",  "CHW") },
    "CLF" to lazy { Currency("CLF", 990, 4, "Unidad de Fomento",  "CLF") },
    "CLP" to lazy { Currency("CLP", 152, 0, "Chilean Peso",  "$") },
    "CNY" to lazy { Currency("CNY", 156, 2, "Yuan Renminbi",  "¥") },
    "COP" to lazy { Currency("COP", 170, 2, "Colombian Peso",  "$") },
    "COU" to lazy { Currency("COU", 970, 2, "Unidad de Valor Real",  "COU") },
    "CRC" to lazy { Currency("CRC", 188, 2, "Costa Rican Colon",  "₡") },
    "CUC" to lazy { Currency("CUC", 931, 2, "Peso Convertible",  "$") },
    "CUP" to lazy { Currency("CUP", 192, 2, "Cuban Peso",  "₱") },
    "CVE" to lazy { Currency("CVE", 132, 2, "Cabo Verde Escudo",  "$") },
    "CZK" to lazy { Currency("CZK", 203, 2, "Czech Koruna",  "Kč") },
    "DJF" to lazy { Currency("DJF", 262, 0, "Djibouti Franc",  "Fdj") },
    "DKK" to lazy { Currency("DKK", 208, 2, "Danish Krone", "kr") },
    "DOP" to lazy { Currency("DOP", 214, 2, "Dominican Peso", "RD$") },
    "DZD" to lazy { Currency("DZD", 12, 2, "Algerian Dinar",  "دج") },
    "EGP" to lazy { Currency("EGP", 818, 2, "Egyptian Pound",  "£") },
    "ERN" to lazy { Currency("ERN", 232, 2, "Nakfa",  "Nfk") },
    "ETB" to lazy { Currency("ETB", 230, 2, "Ethiopian Birr",  "Br") },
    "EUR" to lazy { Currency("EUR", 978, 2, "Euro", "€") },
    "FJD" to lazy { Currency("FJD", 242, 2, "Fiji Dollar",  "$") },
    "FKP" to lazy { Currency("FKP", 238, 2, "Falkland Islands Pound", "£") },
    "GBP" to lazy { Currency("GBP", 826, 2, "Pound Sterling",  "£") },
    "GEL" to lazy { Currency("GEL", 981, 2, "Lari",  "₾") },
    "GHS" to lazy { Currency("GHS", 936, 2, "Ghana Cedi",  "GH₵") },
    "GIP" to lazy { Currency("GIP", 292, 2, "Gibraltar Pound",  "£") },
    "GMD" to lazy { Currency("GMD", 270, 2, "Dalasi",  "D") },
    "GNF" to lazy { Currency("GNF", 324, 0, "Guinean Franc",  "FG") },
    "GTQ" to lazy { Currency("GTQ", 320, 2, "Quetzal",  "Q") },
    "GYD" to lazy { Currency("GYD", 328, 2, "Guyana Dollar",  "$") },
    "HKD" to lazy { Currency("HKD", 344, 2, "Hong Kong Dollar",  "$") },
    "HNL" to lazy { Currency("HNL", 340, 2, "Lempira",  "L") },
    "HTG" to lazy { Currency("HTG", 332, 2, "Gourde",  "G") },
    "HUF" to lazy { Currency("HUF", 348, 2, "Forint",  "Ft") },
    "IDR" to lazy { Currency("IDR", 360, 2, "Rupiah",  "Rp") },
    "ILS" to lazy { Currency("ILS", 376, 2, "New Israeli Sheqel",  "₪") },
    "INR" to lazy { Currency("INR", 356, 2, "Indian Rupee",  "₹") },
    "IQD" to lazy { Currency("IQD", 368, 3, "Iraqi Dinar",  "ع.د") },
    "IRR" to lazy { Currency("IRR", 364, 2, "Iranian Rial",  "﷼") },
    "ISK" to lazy { Currency("ISK", 352, 0, "Iceland Krona",  "kr") },
    "JMD" to lazy { Currency("JMD", 388, 2, "Jamaican Dollar",  "J$") },
    "JOD" to lazy { Currency("JOD", 400, 3, "Jordanian Dinar",  "JD") },
    "JPY" to lazy { Currency("JPY", 392, 0, "Yen",  "¥") },
    "KES" to lazy { Currency("KES", 404, 2, "Kenyan Shilling",  "KSh") },
    "KGS" to lazy { Currency("KGS", 417, 2, "Som",  "лв") },
    "KHR" to lazy { Currency("KHR", 116, 2, "Riel",  "៛") },
    "KMF" to lazy { Currency("KMF", 174, 0, "Comorian Franc ",  "CF") },
    "KPW" to lazy { Currency("KPW", 408, 2, "North Korean Won",  "₩") },
    "KRW" to lazy { Currency("KRW", 410, 0, "Won",  "₩") },
    "KWD" to lazy { Currency("KWD", 414, 3, "Kuwaiti Dinar",  "KD") },
    "KYD" to lazy { Currency("KYD", 136, 2, "Cayman Islands Dollar",  "$") },
    "KZT" to lazy { Currency("KZT", 398, 2, "Tenge",  "₸") },
    "LAK" to lazy { Currency("LAK", 418, 2, "Lao Kip",  "₭") },
    "LBP" to lazy { Currency("LBP", 422, 2, "Lebanese Pound",  "£") },
    "LKR" to lazy { Currency("LKR", 144, 2, "Sri Lanka Rupee",  "₨") },
    "LRD" to lazy { Currency("LRD", 430, 2, "Liberian Dollar",  "$") },
    "LSL" to lazy { Currency("LSL", 426, 2, "Loti",  "M") },
    "LYD" to lazy { Currency("LYD", 434, 3, "Libyan Dinar",  "LD") },
    "MAD" to lazy { Currency("MAD", 504, 2, "Moroccan Dirham",  "MAD") },
    "MDL" to lazy { Currency("MDL", 498, 2, "Moldovan Leu",  "lei") },
    "MGA" to lazy { Currency("MGA", 969, 2, "Malagasy Ariary",  "Ar") },
    "MKD" to lazy { Currency("MKD", 807, 2, "Denar",  "ден") },
    "MMK" to lazy { Currency("MMK", 104, 2, "Kyat",  "K") },
    "MNT" to lazy { Currency("MNT", 496, 2, "Tugrik",  "₮") },
    "MOP" to lazy { Currency("MOP", 446, 2, "Pataca",  "MOP$") },
    "MRU" to lazy { Currency("MRU", 929, 2, "Ouguiya",  "UM") },
    "MUR" to lazy { Currency("MUR", 480, 2, "Mauritius Rupee",  "₨") },
    "MVR" to lazy { Currency("MVR", 462, 2, "Rufiyaa",  "Rf") },
    "MWK" to lazy { Currency("MWK", 454, 2, "Malawian Kwacha",  "MK") },
    "MXN" to lazy { Currency("MXN", 484, 2, "Mexican Peso",  "$") },
    "MXV" to lazy { Currency("MXV", 979, 2, "Mexican Unidad de Inversion (UDI)",  "MXV") },
    "MYR" to lazy { Currency("MYR", 458, 2, "Malaysian Ringgit",  "RM") },
    "MZN" to lazy { Currency("MZN", 943, 2, "Mozambique Metical",  "MT") },
    "NAD" to lazy { Currency("NAD", 516, 2, "Namibia Dollar",  "$") },
    "NGN" to lazy { Currency("NGN", 566, 2, "Naira",  "₦") },
    "NIO" to lazy { Currency("NIO", 558, 2, "Cordoba Oro",  "C$") },
    "NOK" to lazy { Currency("NOK", 578, 2, "Norwegian Krone",  "kr") },
    "NPR" to lazy { Currency("NPR", 524, 2, "Nepalese Rupee",  "₨") },
    "NZD" to lazy { Currency("NZD", 554, 2, "New Zealand Dollar",  "$") },
    "OMR" to lazy { Currency("OMR", 512, 3, "Omani Rial",  "﷼") },
    "PAB" to lazy { Currency("PAB", 590, 2, "Balboa",  "B/.") },
    "PEN" to lazy { Currency("PEN", 604, 2, "Sol", "S/.") },
    "PGK" to lazy { Currency("PGK", 598, 2, "Kina",  "K") },
    "PHP" to lazy { Currency("PHP", 608, 2, "Philippine Peso",  "₱") },
    "PKR" to lazy { Currency("PKR", 586, 2, "Pakistan Rupee",  "₨") },
    "PLN" to lazy { Currency("PLN", 985, 2, "Zloty",  "zł") },
    "PYG" to lazy { Currency("PYG", 600, 0, "Guarani",  "Gs") },
    "QAR" to lazy { Currency("QAR", 634, 2, "Qatari Riyal",  "﷼") },
    "XCG" to lazy { Currency("XCG", 532, 2, "Caribbean Guilder",  "Cg") },
) .mapValues { it.value.value }
