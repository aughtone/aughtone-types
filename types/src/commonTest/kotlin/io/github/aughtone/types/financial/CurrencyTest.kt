package io.github.aughtone.types.financial

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// XXX Name tests have been excluded because the different platforms sometimes have conflicting data.
class CurrencyTest {
    @Test
    fun `get correct CAD currency`() {
        val currency = currencyFor("CAD")
        assertNotNull(currency)
        assertEquals("CAD", currency.code)
        assertEquals("$", currency.symbol)
//        assertEquals("Canadian Dollar", currency.name)
        assertEquals(124, currency.number)
    }

    @Test
    fun `get correct EUR currency`() {
        val currency = currencyFor("EUR")
        assertNotNull(currency)
        assertEquals("EUR", currency.code)
        assertEquals("€", currency.symbol)
//        assertEquals("Euro", currency.name)
        assertEquals(978, currency.number)
    }

    @Test
    fun `get correct DZD currency`() {
        val currency = currencyFor("DZD")
        assertNotNull(currency)
        assertEquals("DZD", currency.code)
        assertEquals("دج", currency.symbol)
//        assertEquals("Algerian Dinar", currency.name)
        assertEquals(12, currency.number)
    }

    @Test
    fun `get correct JPY currency`() {
        val currency = currencyFor("JPY")
        assertNotNull(currency)
        assertEquals("JPY", currency.code)
        assertEquals("¥", currency.symbol)
//        assertEquals("Yen", currency.name)
        assertEquals(392, currency.number)
    }

    @Test
    fun `get correct AED currency`() {
        val currency = currencyFor("AED")
        assertNotNull(currency)
        assertEquals("AED", currency.code)
        assertEquals("د.إ", currency.symbol)
//        assertEquals("UAE Dirham", currency.name)
        assertEquals(784, currency.number)
    }

    @Test
    fun `get correct AFN currency`() {
        val currency = currencyFor("AFN")
        assertNotNull(currency)
        assertEquals("AFN", currency.code)
        assertEquals("؋", currency.symbol)
//        assertEquals("Afghani", currency.name)
        assertEquals(971, currency.number)
    }

    @Test
    fun `get correct ALL currency`() {
        val currency = currencyFor("ALL")
        assertNotNull(currency)
        assertEquals("ALL", currency.code)
        assertEquals("L", currency.symbol)
//        assertEquals("Lek", currency.name)
        assertEquals(8, currency.number)
    }

    @Test
    fun `get correct AMD currency`() {
        val currency = currencyFor("AMD")
        assertNotNull(currency)
        assertEquals("AMD", currency.code)
        assertEquals("֏", currency.symbol)
//        assertEquals("Armenian Dram", currency.name)
        assertEquals(51, currency.number)
    }

    @Test
    fun `get correct ANG currency`() {
        val currency = currencyFor("ANG")
        assertNotNull(currency)
        assertEquals("ANG", currency.code)
        assertEquals("ƒ", currency.symbol)
//        assertEquals("Netherlands Antillean Guilder", currency.name)
        assertEquals(532, currency.number)
    }

    @Test
    fun `get correct AOA currency`() {
        val currency = currencyFor("AOA")
        assertNotNull(currency)
        assertEquals("AOA", currency.code)
        assertEquals("Kz", currency.symbol)
//        assertEquals("Kwanza", currency.name)
        assertEquals(973, currency.number)
    }

    @Test
    fun `get correct ARS currency`() {
        val currency = currencyFor("ARS")
        assertNotNull(currency)
        assertEquals("ARS", currency.code)
        assertEquals("$", currency.symbol)
//        assertEquals("Argentine Peso", currency.name)
        assertEquals(32, currency.number)
    }

    @Test
    fun `get correct AUD currency`() {
        val currency = currencyFor("AUD")
        assertNotNull(currency)
        assertEquals("AUD", currency.code)
        assertEquals("$", currency.symbol)
//        assertEquals("Australian Dollar", currency.name)
        assertEquals(36, currency.number)
    }

    @Test
    fun `get correct AWG currency`() {
        val currency = currencyFor("AWG")
        assertNotNull(currency)
        assertEquals("AWG", currency.code)
        assertEquals("ƒ", currency.symbol)
//        assertEquals("Aruban Florin", currency.name)
        assertEquals(533, currency.number)
    }

    @Test
    fun `get correct AZN currency`() {
        val currency = currencyFor("AZN")
        assertNotNull(currency)
        assertEquals("AZN", currency.code)
        assertEquals("₼", currency.symbol)
//        assertEquals("Azerbaijan Manat", currency.name)
        assertEquals(944, currency.number)
    }

    @Test
    fun `get correct BAM currency`() {
        val currency = currencyFor("BAM")
        assertNotNull(currency)
        assertEquals("BAM", currency.code)
        assertEquals("KM", currency.symbol)
//        assertEquals("Convertible Mark", currency.name)
        assertEquals(977, currency.number)
    }

    @Test
    fun `get correct BBD currency`() {
        val currency = currencyFor("BBD")
        assertNotNull(currency)
        assertEquals("BBD", currency.code)
        assertEquals("$", currency.symbol)
//        assertEquals("Barbados Dollar", currency.name)
        assertEquals(52, currency.number)
    }

    @Test
    fun `get correct BDT currency`() {
        val currency = currencyFor("BDT")
        assertNotNull(currency)
        assertEquals("BDT", currency.code)
        assertEquals("৳", currency.symbol)
//        assertEquals("Taka", currency.name)
        assertEquals(50, currency.number)
    }

    @Test
    fun `get correct BGN currency`() {
        val currency = currencyFor("BGN")
        assertNotNull(currency)
        assertEquals("BGN", currency.code)
        assertEquals("лв", currency.symbol)
//        assertEquals("Bulgarian Lev", currency.name)
        assertEquals(975, currency.number)
    }

    @Test
    fun `get correct BHD currency`() {
        val currency = currencyFor("BHD")
        assertNotNull(currency)
        assertEquals("BHD", currency.code)
        assertEquals(".د.ب", currency.symbol)
//        assertEquals("Bahraini Dinar", currency.name)
        assertEquals(48, currency.number)
    }

    @Test
    fun `get correct BIF currency`() {
        val currency = currencyFor("BIF")
        assertNotNull(currency)
        assertEquals("BIF", currency.code)
        assertEquals("FBu", currency.symbol)
//        assertEquals("Burundi Franc", currency.name)
        assertEquals(108, currency.number)
    }

    @Test
    fun `get correct BMD currency`() {
        val currency = currencyFor("BMD")
        assertNotNull(currency)
        assertEquals("BMD", currency.code)
        assertEquals("$", currency.symbol)
//        assertEquals("Bermudian Dollar", currency.name)
        assertEquals(60, currency.number)
    }

    @Test
    fun `get correct BND currency`() {
        val currency = currencyFor("BND")
        assertNotNull(currency)
        assertEquals("BND", currency.code)
        assertEquals("$", currency.symbol)
//        assertEquals("Brunei Dollar", currency.name)
        assertEquals(96, currency.number)
    }

    @Test
    fun `get correct BOB currency`() {
        val currency = currencyFor("BOB")
        assertNotNull(currency)
        assertEquals("BOB", currency.code)
        assertEquals("\$b", currency.symbol)
//        assertEquals("Boliviano", currency.name)
        assertEquals(68, currency.number)
    }

    @Test
    fun `get correct BOV currency`() {
        val currency = currencyFor("BOV")
        assertNotNull(currency)
        assertEquals("BOV", currency.code)
        assertEquals("BOV", currency.symbol)
//        assertEquals("Mvdol", currency.name)
        assertEquals(984, currency.number)
    }

    @Test
    fun `get correct BRL currency`() {
        val currency = currencyFor("BRL")
        assertNotNull(currency)
        assertEquals("BRL", currency.code)
        assertEquals("R$", currency.symbol)
//        assertEquals("Brazilian Real", currency.name)
        assertEquals(986, currency.number)
    }

    @Test
    fun `get correct BSD currency`() {
        val currency = currencyFor("BSD")
        assertNotNull(currency)
        assertEquals("BSD", currency.code)
        assertEquals("$", currency.symbol)
//        assertEquals("Bahamian Dollar", currency.name)
        assertEquals(44, currency.number)
    }

    @Test
    fun `get correct BTN currency`() {
        val currency = currencyFor("BTN")
        assertNotNull(currency)
        assertEquals("BTN", currency.code)
        assertEquals("Nu.", currency.symbol)
//        assertEquals("Ngultrum", currency.name)
        assertEquals(64, currency.number)
    }

    @Test
    fun `get correct BWP currency`() {
        val currency = currencyFor("BWP")
        assertNotNull(currency)
        assertEquals("BWP", currency.code)
        assertEquals("P", currency.symbol)
//        assertEquals("Pula", currency.name)
        assertEquals(72, currency.number)
    }

    @Test
    fun `get correct BYN currency`() {
        val currency = currencyFor("BYN")
        assertNotNull(currency)
        assertEquals("BYN", currency.code)
        assertEquals("Br", currency.symbol)
//        assertEquals("Belarusian Ruble", currency.name)
        assertEquals(933, currency.number)
    }

    @Test
    fun `get correct BZD currency`() {
        val currency = currencyFor("BZD")
        assertNotNull(currency)
        assertEquals("BZD", currency.code)
        assertEquals("BZ$", currency.symbol)
//        assertEquals("Belize Dollar", currency.name)
        assertEquals(84, currency.number)
    }

    @Test
    fun `get correct CDF currency`() {
        val currency = currencyFor("CDF")
        assertNotNull(currency)
        assertEquals("CDF", currency.code)
        assertEquals("FC", currency.symbol)
//        assertEquals("Congolese Franc", currency.name)
        assertEquals(976, currency.number)
    }

    @Test
    fun `get correct CHE currency`() {
        val currency = currencyFor("CHE")
        assertNotNull(currency)
        assertEquals("CHE", currency.code)
        assertEquals("CHE", currency.symbol)
//        assertEquals("WIR Euro", currency.name)
        assertEquals(947, currency.number)
    }

    @Test
    fun `get correct CHF currency`() {
        val currency = currencyFor("CHF")
        assertNotNull(currency)
        assertEquals("CHF", currency.code)
        assertEquals("CHF", currency.symbol)
//        assertEquals("Swiss Franc", currency.name)
        assertEquals(756, currency.number)
    }

    @Test
    fun `get correct CHW currency`() {
        val currency = currencyFor("CHW")
        assertNotNull(currency)
        assertEquals("CHW", currency.code)
        assertEquals("CHW", currency.symbol)
//        assertEquals("WIR Franc", currency.name)
        assertEquals(948, currency.number)
    }

    @Test
    fun `get correct CLF currency`() {
        val currency = currencyFor("CLF")
        assertNotNull(currency)
        assertEquals("CLF", currency.code)
        assertEquals("CLF", currency.symbol)
//        assertEquals("Unidad de Fomento", currency.name)
        assertEquals(990, currency.number)
    }

    @Test
    fun `get correct CLP currency`() {
        val currency = currencyFor("CLP")
        assertNotNull(currency)
        assertEquals("CLP", currency.code)
        assertEquals("$", currency.symbol)
//        assertEquals("Chilean Peso", currency.name)
        assertEquals(152, currency.number)
    }

    @Test
    fun `get correct CNY currency`() {
        val currency = currencyFor("CNY")
        assertNotNull(currency)
        assertEquals("CNY", currency.code)
        assertEquals("¥", currency.symbol)
//        assertEquals("Yuan Renminbi", currency.name)
        assertEquals(156, currency.number)
    }

    @Test
    fun `get correct COP currency`() {
        val currency = currencyFor("COP")
        assertNotNull(currency)
        assertEquals("COP", currency.code)
        assertEquals("$", currency.symbol)
//        assertEquals("Colombian Peso", currency.name)
        assertEquals(170, currency.number)
    }

    @Test
    fun `get correct COU currency`() {
        val currency = currencyFor("COU")
        assertNotNull(currency)
        assertEquals("COU", currency.code)
        assertEquals("COU", currency.symbol)
//        assertEquals("Unidad de Valor Real", currency.name)
        assertEquals(970, currency.number)
    }

    @Test
    fun `get correct CRC currency`() {
        val currency = currencyFor("CRC")
        assertNotNull(currency)
        assertEquals("CRC", currency.code)
        assertEquals("₡", currency.symbol)
//        assertEquals("Costa Rican Colon", currency.name)
        assertEquals(188, currency.number)
    }

    @Test
    fun `get correct CUC currency`() {
        val currency = currencyFor("CUC")
        assertNotNull(currency)
        assertEquals("CUC", currency.code)
        assertEquals("$", currency.symbol)
//        assertEquals("Peso Convertible", currency.name) // platform inconsistency on android: Cuban Convertible Peso
        assertEquals(931, currency.number)
    }

    @Test
    fun `get correct CUP currency`() {
        val currency = currencyFor("CUP")
        assertNotNull(currency)
        assertEquals("CUP", currency.code)
        assertEquals("₱", currency.symbol)
//        assertEquals("Cuban Peso", currency.name)
        assertEquals(192, currency.number)
    }

    @Test
    fun `get correct CVE currency`() {
        val currency = currencyFor("CVE")
        assertNotNull(currency)
        assertEquals("CVE", currency.code)
        assertEquals("$", currency.symbol)
//        assertEquals("Cabo Verde Escudo", currency.name)
        assertEquals(132, currency.number)
    }

    @Test
    fun `get correct CZK currency`() {
        val currency = currencyFor("CZK")
        assertNotNull(currency)
        assertEquals("CZK", currency.code)
        assertEquals("Kč", currency.symbol)
//        assertEquals("Czech Koruna", currency.name)
        assertEquals(203, currency.number)
    }

    @Test
    fun `get correct DJF currency`() {
        val currency = currencyFor("DJF")
        assertNotNull(currency)
        assertEquals("DJF", currency.code)
        assertEquals("Fdj", currency.symbol)
//        assertEquals("Djibouti Franc", currency.name)
        assertEquals(262, currency.number)
    }

    @Test
    fun `get correct DKK currency`() {
        val currency = currencyFor("DKK")
        assertNotNull(currency)
        assertEquals("DKK", currency.code)
        assertEquals("kr", currency.symbol)
//        assertEquals("Danish Krone", currency.name)
        assertEquals(208, currency.number)
    }

    @Test
    fun `get correct DOP currency`() {
        val currency = currencyFor("DOP")
        assertNotNull(currency)
        assertEquals("DOP", currency.code)
        assertEquals("RD$", currency.symbol)
//        assertEquals("Dominican Peso", currency.name)
        assertEquals(214, currency.number)
    }

    @Test
    fun `get correct EGP currency`() {
        val currency = currencyFor("EGP")
        assertNotNull(currency)
        assertEquals("EGP", currency.code)
        assertEquals("£", currency.symbol)
//        assertEquals("Egyptian Pound", currency.name)
        assertEquals(818, currency.number)
    }

    @Test
    fun `get correct ERN currency`() {
        val currency = currencyFor("ERN")
        assertNotNull(currency)
        assertEquals("ERN", currency.code)
        assertEquals("Nfk", currency.symbol)
//        assertEquals("Nakfa", currency.name)
        assertEquals(232, currency.number)
    }

    @Test
    fun `get correct ETB currency`() {
        val currency = currencyFor("ETB")
        assertNotNull(currency)
        assertEquals("ETB", currency.code)
        assertEquals("Br", currency.symbol)
//        assertEquals("Ethiopian Birr", currency.name)
        assertEquals(230, currency.number)
    }

    @Test
    fun `get correct FJD currency`() {
        val currency = currencyFor("FJD")
        assertNotNull(currency)
        assertEquals("FJD", currency.code)
        assertEquals("$", currency.symbol)
//        assertEquals("Fiji Dollar", currency.name)
        assertEquals(242, currency.number)
    }

    @Test
    fun `get correct FKP currency`() {
        val currency = currencyFor("FKP")
        assertNotNull(currency)
        assertEquals("FKP", currency.code)
        assertEquals("£", currency.symbol)
//        assertEquals("Falkland Islands Pound", currency.name)
        assertEquals(238, currency.number)
    }

    @Test
    fun `get correct GBP currency`() {
        val currency = currencyFor("GBP")
        assertNotNull(currency)
        assertEquals("GBP", currency.code)
        assertEquals("£", currency.symbol)
//        assertEquals("Pound Sterling", currency.name)
        assertEquals(826, currency.number)
    }

    @Test
    fun `get correct GEL currency`() {
        val currency = currencyFor("GEL")
        assertNotNull(currency)
        assertEquals("GEL", currency.code)
        assertEquals("₾", currency.symbol)
//        assertEquals("Lari", currency.name)
        assertEquals(981, currency.number)
    }

    @Test
    fun `get correct GHS currency`() {
        val currency = currencyFor("GHS")
        assertNotNull(currency)
        assertEquals("GHS", currency.code)
        assertEquals("GH₵", currency.symbol)
//        assertEquals("Ghana Cedi", currency.name)
        assertEquals(936, currency.number)
    }

    @Test
    fun `get correct GIP currency`() {
        val currency = currencyFor("GIP")
        assertNotNull(currency)
        assertEquals("GIP", currency.code)
        assertEquals("£", currency.symbol)
//        assertEquals("Gibraltar Pound", currency.name)
        assertEquals(292, currency.number)
    }

    @Test
    fun `get correct GMD currency`() {
        val currency = currencyFor("GMD")
        assertNotNull(currency)
        assertEquals("GMD", currency.code)
        assertEquals("D", currency.symbol)
//        assertEquals("Dalasi", currency.name)
        assertEquals(270, currency.number)
    }

    @Test
    fun `get correct GNF currency`() {
        val currency = currencyFor("GNF")
        assertNotNull(currency)
        assertEquals("GNF", currency.code)
        assertEquals("FG", currency.symbol)
//        assertEquals("Guinean Franc", currency.name)
        assertEquals(324, currency.number)
    }

    @Test
    fun `get correct GTQ currency`() {
        val currency = currencyFor("GTQ")
        assertNotNull(currency)
        assertEquals("GTQ", currency.code)
        assertEquals("Q", currency.symbol)
//        assertEquals("Quetzal", currency.name)
        assertEquals(320, currency.number)
    }

    @Test
    fun `get correct GYD currency`() {
        val currency = currencyFor("GYD")
        assertNotNull(currency)
        assertEquals("GYD", currency.code)
        assertEquals("$", currency.symbol)
//        assertEquals("Guyana Dollar", currency.name)
        assertEquals(328, currency.number)
    }

    @Test
    fun `get correct HKD currency`() {
        val currency = currencyFor("HKD")
        assertNotNull(currency)
        assertEquals("HKD", currency.code)
        assertEquals("$", currency.symbol)
//        assertEquals("Hong Kong Dollar", currency.name)
        assertEquals(344, currency.number)
    }

    @Test
    fun `get correct HNL currency`() {
        val currency = currencyFor("HNL")
        assertNotNull(currency)
        assertEquals("HNL", currency.code)
        assertEquals("L", currency.symbol)
//        assertEquals("Lempira", currency.name)
        assertEquals(340, currency.number)
    }

    @Test
    fun `get correct HTG currency`() {
        val currency = currencyFor("HTG")
        assertNotNull(currency)
        assertEquals("HTG", currency.code)
        assertEquals("G", currency.symbol)
//        assertEquals("Gourde", currency.name)
        assertEquals(332, currency.number)
    }

    @Test
    fun `get correct HUF currency`() {
        val currency = currencyFor("HUF")
        assertNotNull(currency)
        assertEquals("HUF", currency.code)
        assertEquals("Ft", currency.symbol)
//        assertEquals("Forint", currency.name) // Platform inconsistency "Hungarian Forint" on Android
        assertEquals(348, currency.number)
    }

    @Test
    fun `get correct IDR currency`() {
        val currency = currencyFor("IDR")
        assertNotNull(currency)
        assertEquals("IDR", currency.code)
        assertEquals("Rp", currency.symbol)
//        assertEquals("Rupiah", currency.name)
        assertEquals(360, currency.number)
    }

    @Test
    fun `get correct ILS currency`() {
        val currency = currencyFor("ILS")
        assertNotNull(currency)
        assertEquals("ILS", currency.code)
        assertEquals("₪", currency.symbol)
//        assertEquals("New Israeli Sheqel", currency.name)
        assertEquals(376, currency.number)
    }

    @Test
    fun `get correct INR currency`() {
        val currency = currencyFor("INR")
        assertNotNull(currency)
        assertEquals("INR", currency.code)
        assertEquals("₹", currency.symbol)
//        assertEquals("Indian Rupee", currency.name)
        assertEquals(356, currency.number)
    }

    @Test
    fun `get correct IQD currency`() {
        val currency = currencyFor("IQD")
        assertNotNull(currency)
        assertEquals("IQD", currency.code)
        assertEquals("ع.د", currency.symbol)
//        assertEquals("Iraqi Dinar", currency.name)
        assertEquals(368, currency.number)
    }

    @Test
    fun `get correct IRR currency`() {
        val currency = currencyFor("IRR")
        assertNotNull(currency)
        assertEquals("IRR", currency.code)
        assertEquals("﷼", currency.symbol)
//        assertEquals("Iranian Rial", currency.name)
        assertEquals(364, currency.number)
    }

    @Test
    fun `get correct ISK currency`() {
        val currency = currencyFor("ISK")
        assertNotNull(currency)
        assertEquals("ISK", currency.code)
        assertEquals("kr", currency.symbol)
//        assertEquals("Iceland Krona", currency.name)
        assertEquals(352, currency.number)
    }

    @Test
    fun `get correct JMD currency`() {
        val currency = currencyFor("JMD")
        assertNotNull(currency)
        assertEquals("JMD", currency.code)
        assertEquals("J$", currency.symbol)
//        assertEquals("Jamaican Dollar", currency.name)
        assertEquals(388, currency.number)
    }

    @Test
    fun `get correct JOD currency`() {
        val currency = currencyFor("JOD")
        assertNotNull(currency)
        assertEquals("JOD", currency.code)
        assertEquals("JD", currency.symbol)
//        assertEquals("Jordanian Dinar", currency.name)
        assertEquals(400, currency.number)
    }

    @Test
    fun `get correct KES currency`() {
        val currency = currencyFor("KES")
        assertNotNull(currency)
        assertEquals("KES", currency.code)
        assertEquals("KSh", currency.symbol)
//        assertEquals("Kenyan Shilling", currency.name)
        assertEquals(404, currency.number)
    }

    @Test
    fun `get correct KGS currency`() {
        val currency = currencyFor("KGS")
        assertNotNull(currency)
        assertEquals("KGS", currency.code)
        assertEquals("лв", currency.symbol)
//        assertEquals("Som", currency.name)
        assertEquals(417, currency.number)
    }

    @Test
    fun `get correct KHR currency`() {
        val currency = currencyFor("KHR")
        assertNotNull(currency)
        assertEquals("KHR", currency.code)
        assertEquals("៛", currency.symbol)
//        assertEquals("Riel", currency.name) // Android: "Cambodian Riel"
        assertEquals(116, currency.number)
    }

    @Test
    fun `get correct KMF currency`() {
        val currency = currencyFor("KMF")
        assertNotNull(currency)
        assertEquals("KMF", currency.code)
        assertEquals("CF", currency.symbol)
//        assertEquals("Comorian Franc ", currency.name)
        assertEquals(174, currency.number)
    }

    @Test
    fun `get correct KPW currency`() {
        val currency = currencyFor("KPW")
        assertNotNull(currency)
        assertEquals("KPW", currency.code)
        assertEquals("₩", currency.symbol)
//        assertEquals("North Korean Won", currency.name)
        assertEquals(408, currency.number)
    }

    @Test
    fun `get correct KRW currency`() {
        val currency = currencyFor("KRW")
        assertNotNull(currency)
        assertEquals("KRW", currency.code)
        assertEquals("₩", currency.symbol)
//        assertEquals("Won", currency.name)
        assertEquals(410, currency.number)
    }

    @Test
    fun `get correct KWD currency`() {
        val currency = currencyFor("KWD")
        assertNotNull(currency)
        assertEquals("KWD", currency.code)
        assertEquals("KD", currency.symbol)
//        assertEquals("Kuwaiti Dinar", currency.name)
        assertEquals(414, currency.number)
    }

    @Test
    fun `get correct KYD currency`() {
        val currency = currencyFor("KYD")
        assertNotNull(currency)
        assertEquals("KYD", currency.code)
        assertEquals("$", currency.symbol)
        assertEquals("Cayman Islands Dollar", currency.name)
        assertEquals(136, currency.number)
    }

    @Test
    fun `get correct KZT currency`() {
        val currency = currencyFor("KZT")
        assertNotNull(currency)
        assertEquals("KZT", currency.code)
        assertEquals("₸", currency.symbol)
//        assertEquals("Tenge", currency.name)
        assertEquals(398, currency.number)
    }

    @Test
    fun `get correct LAK currency`() {
        val currency = currencyFor("LAK")
        assertNotNull(currency)
        assertEquals("LAK", currency.code)
        assertEquals("₭", currency.symbol)
//        assertEquals("Lao Kip", currency.name)
        assertEquals(418, currency.number)
    }

    @Test
    fun `get correct LBP currency`() {
        val currency = currencyFor("LBP")
        assertNotNull(currency)
        assertEquals("LBP", currency.code)
        assertEquals("£", currency.symbol)
//        assertEquals("Lebanese Pound", currency.name)
        assertEquals(422, currency.number)
    }

    @Test
    fun `get correct LKR currency`() {
        val currency = currencyFor("LKR")
        assertNotNull(currency)
        assertEquals("LKR", currency.code)
        assertEquals("₨", currency.symbol)
//        assertEquals("Sri Lanka Rupee", currency.name)
        assertEquals(144, currency.number)
    }

    @Test
    fun `get correct LRD currency`() {
        val currency = currencyFor("LRD")
        assertNotNull(currency)
        assertEquals("LRD", currency.code)
        assertEquals("$", currency.symbol)
//        assertEquals("Liberian Dollar", currency.name)
        assertEquals(430, currency.number)
    }

    @Test
    fun `get correct LSL currency`() {
        val currency = currencyFor("LSL")
        assertNotNull(currency)
        assertEquals("LSL", currency.code)
        assertEquals("M", currency.symbol)
//        assertEquals("Loti", currency.name)
        assertEquals(426, currency.number)
    }

    @Test
    fun `get correct LYD currency`() {
        val currency = currencyFor("LYD")
        assertNotNull(currency)
        assertEquals("LYD", currency.code)
        assertEquals("LD", currency.symbol)
//        assertEquals("Libyan Dinar", currency.name)
        assertEquals(434, currency.number)
    }

    @Test
    fun `get correct MAD currency`() {
        val currency = currencyFor("MAD")
        assertNotNull(currency)
        assertEquals("MAD", currency.code)
        assertEquals("MAD", currency.symbol)
        assertEquals("Moroccan Dirham", currency.name)
        assertEquals(504, currency.number)
    }

    @Test
    fun `get correct MDL currency`() {
        val currency = currencyFor("MDL")
        assertNotNull(currency)
        assertEquals("MDL", currency.code)
        assertEquals("lei", currency.symbol)
        assertEquals("Moldovan Leu", currency.name)
        assertEquals(498, currency.number)
    }

    @Test
    fun `get correct MGA currency`() {
        val currency = currencyFor("MGA")
        assertNotNull(currency)
        assertEquals("MGA", currency.code)
        assertEquals("Ar", currency.symbol)
        assertEquals("Malagasy Ariary", currency.name)
        assertEquals(969, currency.number)
    }

    @Test
    fun `get correct MKD currency`() {
        val currency = currencyFor("MKD")
        assertNotNull(currency)
        assertEquals("MKD", currency.code)
        assertEquals("ден", currency.symbol)
//        assertEquals("Denar", currency.name)
        assertEquals(807, currency.number)
    }

    @Test
    fun `get correct MMK currency`() {
        val currency = currencyFor("MMK")
        assertNotNull(currency)
        assertEquals("MMK", currency.code)
        assertEquals("K", currency.symbol)
//        assertEquals("Kyat", currency.name)
        assertEquals(104, currency.number)
    }

    @Test
    fun `get correct MNT currency`() {
        val currency = currencyFor("MNT")
        assertNotNull(currency)
        assertEquals("MNT", currency.code)
        assertEquals("₮", currency.symbol)
//        assertEquals("Tugrik", currency.name)
        assertEquals(496, currency.number)
    }

    @Test
    fun `get correct MOP currency`() {
        val currency = currencyFor("MOP")
        assertNotNull(currency)
        assertEquals("MOP", currency.code)
        assertEquals("MOP$", currency.symbol)
//        assertEquals("Pataca", currency.name)
        assertEquals(446, currency.number)
    }

    @Test
    fun `get correct MRU currency`() {
        val currency = currencyFor("MRU")
        assertNotNull(currency)
        assertEquals("MRU", currency.code)
        assertEquals("UM", currency.symbol)
//        assertEquals("Ouguiya", currency.name)
        assertEquals(929, currency.number)
    }

    @Test
    fun `get correct MUR currency`() {
        val currency = currencyFor("MUR")
        assertNotNull(currency)
        assertEquals("MUR", currency.code)
        assertEquals("₨", currency.symbol)
//        assertEquals("Mauritius Rupee", currency.name)
        assertEquals(480, currency.number)
    }

    @Test
    fun `get correct MVR currency`() {
        val currency = currencyFor("MVR")
        assertNotNull(currency)
        assertEquals("MVR", currency.code)
        assertEquals("Rf", currency.symbol)
//        assertEquals("Rufiyaa", currency.name)
        assertEquals(462, currency.number)
    }

    @Test
    fun `get correct MWK currency`() {
        val currency = currencyFor("MWK")
        assertNotNull(currency)
        assertEquals("MWK", currency.code)
        assertEquals("MK", currency.symbol)
//        assertEquals("Malawian Kwacha", currency.name)
        assertEquals(454, currency.number)
    }

    @Test
    fun `get correct MXN currency`() {
        val currency = currencyFor("MXN")
        assertNotNull(currency)
        assertEquals("MXN", currency.code)
        assertEquals("$", currency.symbol)
        assertEquals("Mexican Peso", currency.name)
        assertEquals(484, currency.number)
    }

    @Test
    fun `get correct MXV currency`() {
        val currency = currencyFor("MXV")
        assertNotNull(currency)
        assertEquals("MXV", currency.code)
        assertEquals("MXV", currency.symbol)
//        assertEquals("Mexican Unidad de Inversion (UDI)", currency.name)
        assertEquals(979, currency.number)
    }

    @Test
    fun `get correct MYR currency`() {
        val currency = currencyFor("MYR")
        assertNotNull(currency)
        assertEquals("MYR", currency.code)
        assertEquals("RM", currency.symbol)
//        assertEquals("Malaysian Ringgit", currency.name)
        assertEquals(458, currency.number)
    }

    @Test
    fun `get correct MZN currency`() {
        val currency = currencyFor("MZN")
        assertNotNull(currency)
        assertEquals("MZN", currency.code)
        assertEquals("MT", currency.symbol)
//        assertEquals("Mozambique Metical", currency.name)
        assertEquals(943, currency.number)
    }

    @Test
    fun `get correct NAD currency`() {
        val currency = currencyFor("NAD")
        assertNotNull(currency)
        assertEquals("NAD", currency.code)
        assertEquals("$", currency.symbol)
//        assertEquals("Namibia Dollar", currency.name)
        assertEquals(516, currency.number)
    }

    @Test
    fun `get correct NGN currency`() {
        val currency = currencyFor("NGN")
        assertNotNull(currency)
        assertEquals("NGN", currency.code)
        assertEquals("₦", currency.symbol)
//        assertEquals("Naira", currency.name)
        assertEquals(566, currency.number)
    }

    @Test
    fun `get correct NIO currency`() {
        val currency = currencyFor("NIO")
        assertNotNull(currency)
        assertEquals("NIO", currency.code)
        assertEquals("C$", currency.symbol)
//        assertEquals("Cordoba Oro", currency.name)
        assertEquals(558, currency.number)
    }

    @Test
    fun `get correct NOK currency`() {
        val currency = currencyFor("NOK")
        assertNotNull(currency)
        assertEquals("NOK", currency.code)
        assertEquals("kr", currency.symbol)
//        assertEquals("Norwegian Krone", currency.name)
        assertEquals(578, currency.number)
    }

    @Test
    fun `get correct NPR currency`() {
        val currency = currencyFor("NPR")
        assertNotNull(currency)
        assertEquals("NPR", currency.code)
        assertEquals("₨", currency.symbol)
        assertEquals("Nepalese Rupee", currency.name)
        assertEquals(524, currency.number)
    }

    @Test
    fun `get correct NZD currency`() {
        val currency = currencyFor("NZD")
        assertNotNull(currency)
        assertEquals("NZD", currency.code)
        assertEquals("$", currency.symbol)
        assertEquals("New Zealand Dollar", currency.name)
        assertEquals(554, currency.number)
    }

    @Test
    fun `get correct OMR currency`() {
        val currency = currencyFor("OMR")
        assertNotNull(currency)
        assertEquals("OMR", currency.code)
        assertEquals("﷼", currency.symbol)
        assertEquals("Omani Rial", currency.name)
        assertEquals(512, currency.number)
    }

    @Test
    fun `get correct PAB currency`() {
        val currency = currencyFor("PAB")
        assertNotNull(currency)
        assertEquals("PAB", currency.code)
        assertEquals("B/.", currency.symbol)
//        assertEquals("Balboa", currency.name)
        assertEquals(590, currency.number)
    }

    @Test
    fun `get correct PEN currency`() {
        val currency = currencyFor("PEN")
        assertNotNull(currency)
        assertEquals("PEN", currency.code)
        assertEquals("S/.", currency.symbol)
//        assertEquals("Sol", currency.name)
        assertEquals(604, currency.number)
    }

    @Test
    fun `get correct PGK currency`() {
        val currency = currencyFor("PGK")
        assertNotNull(currency)
        assertEquals("PGK", currency.code)
        assertEquals("K", currency.symbol)
//        assertEquals("Kina", currency.name)
        assertEquals(598, currency.number)
    }

    @Test
    fun `get correct PHP currency`() {
        val currency = currencyFor("PHP")
        assertNotNull(currency)
        assertEquals("PHP", currency.code)
        assertEquals("₱", currency.symbol)
//        assertEquals("Philippine Peso", currency.name)
        assertEquals(608, currency.number)
    }

    @Test
    fun `get correct PKR currency`() {
        val currency = currencyFor("PKR")
        assertNotNull(currency)
        assertEquals("PKR", currency.code)
        assertEquals("₨", currency.symbol)
//        assertEquals("Pakistan Rupee", currency.name)
        assertEquals(586, currency.number)
    }

    @Test
    fun `get correct PLN currency`() {
        val currency = currencyFor("PLN")
        assertNotNull(currency)
        assertEquals("PLN", currency.code)
        assertEquals("zł", currency.symbol)
//        assertEquals("Zloty", currency.name)
        assertEquals(985, currency.number)
    }

    @Test
    fun `get correct PYG currency`() {
        val currency = currencyFor("PYG")
        assertNotNull(currency)
        assertEquals("PYG", currency.code)
        assertEquals("Gs", currency.symbol)
//        assertEquals("Guarani", currency.name)
        assertEquals(600, currency.number)
    }

    @Test
    fun `get correct QAR currency`() {
        val currency = currencyFor("QAR")
        assertNotNull(currency)
        assertEquals("QAR", currency.code)
        assertEquals("﷼", currency.symbol)
//        assertEquals("Qatari Riyal", currency.name)
        assertEquals(634, currency.number)
    }

}
