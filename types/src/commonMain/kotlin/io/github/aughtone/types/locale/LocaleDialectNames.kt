package io.github.aughtone.types.locale

import io.github.aughtone.types.util.LazyMap

/**
 * CLDR's dialect names for shipped locales — "Canadian French", "français canadien", "Flemish" —
 * by display language, for [LanguageDisplay.Dialect].
 *
 * Data source: Unicode CLDR 48.2.0, `cldr-localenames-full`, each display language's
 * `languages.json`, keeping the language-region and language-script keys (`fr-CA`, `zh-Hant`) whose
 * tag is a shipped locale, without `-alt-` forms. The release and its integrity hash are pinned in
 * `docs/reference/README.md`, which also says how to regenerate this file. Do not edit by hand.
 *
 * Bundled on every target because the platforms cannot be relied on for it: the JVM has no dialect
 * form at all. Packed as one string per display language, `tag\u0001name…` sorted by tag, and parsed
 * on first use for that language only, as `LocaleDisplayNameSupplement` is.
 */
internal object LocaleDialectNames {
    private val packed: Map<String, String> = mapOf(
        "af" to
            "nl-BE\u0001Vlaams",
        "am" to
            "de-AT\u0001የኦስትሪያ ጀርመንኛ\u0001de-CH\u0001የስዊዝ ከፍተኛ ጀርመንኛ\u0001en-AU\u0001የአውስትራሊያ እንግሊዝኛ\u0001en-CA\u0001የካናዳ እንግሊዝኛ\u0001en-GB\u0001የብሪቲሽ እንግሊዝኛ\u0001en-US\u0001የአሜሪካ እንግሊዝኛ\u0001es-ES\u0001የአውሮፓ ስፓኒሽ\u0001es-MX\u0001የሜክሲኮ ስፓኒሽ\u0001fr-CA\u0001የካናዳ ፈረንሳይኛ\u0001fr-CH\u0001የስዊዝ ፈረንሳይኛ\u0001nl-BE\u0001ፍሌሚሽ\u0001pt-BR\u0001የብራዚል ፖርቹጋልኛ\u0001pt-PT\u0001የአውሮፓ ፖርቹጋልኛ\u0001zh-Hans\u0001ቀለል ያለ ቻይንኛ\u0001zh-Hant\u0001ባህላዊ ቻይንኛ",
        "ar" to
            "de-AT\u0001الألمانية النمساوية\u0001es-ES\u0001الإسبانية الأوروبية\u0001es-MX\u0001الإسبانية المكسيكية\u0001pt-BR\u0001البرتغالية البرازيلية\u0001pt-PT\u0001البرتغالية الأوروبية\u0001zh-Hans\u0001الصينية المبسطة\u0001zh-Hant\u0001الصينية التقليدية",
        "as" to
            "de-AT\u0001অষ্ট্ৰেলিয়ান জাৰ্মান\u0001de-CH\u0001ছুইচ হাই জাৰ্মান\u0001en-AU\u0001অষ্ট্ৰেলিয়ান ইংৰাজী\u0001en-CA\u0001কানাডিয়ান ইংৰাজী\u0001en-GB\u0001ব্ৰিটিছ ইংৰাজী\u0001en-US\u0001আমেৰিকান ইংৰাজী\u0001es-ES\u0001ইউৰোপীয়ান স্পেনিচ\u0001es-MX\u0001মেক্সিকান স্পেনিচ\u0001fr-CA\u0001কানাডিয়ান ফ্ৰেন্স\u0001fr-CH\u0001ছুইচ ফ্ৰেন্স\u0001nl-BE\u0001ফ্লেমিচ\u0001pt-BR\u0001ব্ৰাজিলিয়ান পৰ্তুগীজ\u0001pt-PT\u0001ইউৰোপীয়ান পৰ্তুগীজ\u0001zh-Hans\u0001সৰলীকৃত চীনা\u0001zh-Hant\u0001পৰম্পৰাগত চীনা",
        "az" to
            "de-AT\u0001Avstriya almancası\u0001en-AU\u0001Avstraliya ingiliscəsi\u0001en-CA\u0001Kanada ingiliscəsi\u0001en-GB\u0001Britaniya ingiliscəsi\u0001en-US\u0001Amerika ingiliscəsi\u0001es-ES\u0001Kastiliya ispancası\u0001es-MX\u0001Meksika ispancası\u0001fr-CA\u0001Kanada fransızcası\u0001fr-CH\u0001İsveçrə fransızcası\u0001nl-BE\u0001flamand\u0001pt-BR\u0001Braziliya portuqalcası\u0001pt-PT\u0001Portuqaliya portuqalcası\u0001zh-Hans\u0001sadələşmiş çin\u0001zh-Hant\u0001ənənəvi çin",
        "be" to
            "de-AT\u0001аўстрыйская нямецкая\u0001de-CH\u0001швейцарская літаратурная нямецкая\u0001en-AU\u0001аўстралійская англійская\u0001en-CA\u0001канадская англійская\u0001en-GB\u0001брытанская англійская\u0001en-US\u0001амерыканская англійская\u0001es-ES\u0001еўрапейская іспанская\u0001es-MX\u0001мексіканская іспанская\u0001fr-CA\u0001канадская французская\u0001fr-CH\u0001швейцарская французская\u0001nl-BE\u0001фламандская\u0001pt-BR\u0001бразільская партугальская\u0001pt-PT\u0001еўрапейская партугальская\u0001zh-Hans\u0001кітайская (спрошчаныя іерогліфы)\u0001zh-Hant\u0001кітайская (традыцыйныя іерогліфы)",
        "bg" to
            "en-AU\u0001австралийски английски\u0001en-CA\u0001канадски английски\u0001en-GB\u0001британски английски\u0001en-US\u0001американски английски\u0001nl-BE\u0001фламандски\u0001zh-Hans\u0001китайски (опростен)\u0001zh-Hant\u0001китайски (традиционен)",
        "bn" to
            "es-ES\u0001স্প্যানিশ (ইউরোপ)\u0001nl-BE\u0001ফ্লেমিশ\u0001pt-BR\u0001পর্তুগিজ (ব্রাজিল)\u0001pt-PT\u0001পর্তুগিজ (ইউরোপ)",
        "bo" to
            "en-CA\u0001དབྱིན་ཇིའི་སྐད། (ཁེ་ན་ཌ་)\u0001en-GB\u0001དབྱིན་ཇིའི་སྐད། (དབྱིན་ལན་)\u0001en-US\u0001དབྱིན་ཇིའི་སྐད། (ཨ་རི་)",
        "ca" to
            "de-AT\u0001alemany austríac\u0001de-CH\u0001alemany estàndard suís\u0001en-AU\u0001anglès australià\u0001en-CA\u0001anglès canadenc\u0001en-GB\u0001anglès britànic\u0001en-US\u0001anglès americà\u0001es-ES\u0001espanyol europeu\u0001es-MX\u0001espanyol de Mèxic\u0001fr-CA\u0001francès canadenc\u0001fr-CH\u0001francès suís\u0001nl-BE\u0001flamenc\u0001pt-BR\u0001portuguès del Brasil\u0001pt-PT\u0001portuguès de Portugal\u0001zh-Hans\u0001xinès simplificat\u0001zh-Hant\u0001xinès tradicional",
        "cs" to
            "de-CH\u0001němčina standardní (Švýcarsko)\u0001en-GB\u0001angličtina (Velká Británie)\u0001en-US\u0001angličtina (USA)\u0001es-ES\u0001španělština (Evropa)\u0001nl-BE\u0001vlámština\u0001pt-PT\u0001portugalština (Evropa)\u0001zh-Hans\u0001čínština (zjednodušená)",
        "da" to
            "de-AT\u0001østrigsk tysk\u0001de-CH\u0001schweizerhøjtysk\u0001en-AU\u0001australsk engelsk\u0001en-CA\u0001canadisk engelsk\u0001en-GB\u0001britisk engelsk\u0001en-US\u0001amerikansk engelsk\u0001es-ES\u0001europæisk spansk\u0001es-MX\u0001mexicansk spansk\u0001fr-CA\u0001canadisk fransk\u0001fr-CH\u0001schweizisk fransk\u0001nl-BE\u0001flamsk\u0001pt-BR\u0001brasiliansk portugisisk\u0001pt-PT\u0001europæisk portugisisk\u0001zh-Hans\u0001forenklet kinesisk\u0001zh-Hant\u0001traditionelt kinesisk",
        "de" to
            "de-AT\u0001Österreichisches Deutsch\u0001de-CH\u0001Schweizer Hochdeutsch\u0001nl-BE\u0001Flämisch\u0001zh-Hans\u0001Chinesisch (vereinfacht)\u0001zh-Hant\u0001Chinesisch (traditionell)",
        "el" to
            "de-AT\u0001Γερμανικά Αυστρίας\u0001de-CH\u0001Υψηλά Γερμανικά Ελβετίας\u0001en-AU\u0001Αγγλικά Αυστραλίας\u0001en-CA\u0001Αγγλικά Καναδά\u0001en-GB\u0001Αγγλικά Βρετανίας\u0001en-US\u0001Αγγλικά Αμερικής\u0001es-ES\u0001Ισπανικά Ευρώπης\u0001es-MX\u0001Ισπανικά Μεξικού\u0001fr-CA\u0001Γαλλικά Καναδά\u0001fr-CH\u0001Γαλλικά Ελβετίας\u0001nl-BE\u0001Φλαμανδικά\u0001pt-BR\u0001Πορτογαλικά Βραζιλίας\u0001pt-PT\u0001Πορτογαλικά Ευρώπης\u0001zh-Hans\u0001Απλοποιημένα Κινεζικά\u0001zh-Hant\u0001Παραδοσιακά Κινεζικά",
        "en" to
            "de-AT\u0001Austrian German\u0001de-CH\u0001Swiss High German\u0001en-AU\u0001Australian English\u0001en-CA\u0001Canadian English\u0001en-GB\u0001British English\u0001en-US\u0001American English\u0001es-ES\u0001European Spanish\u0001es-MX\u0001Mexican Spanish\u0001fr-CA\u0001Canadian French\u0001fr-CH\u0001Swiss French\u0001nl-BE\u0001Flemish\u0001pt-BR\u0001Brazilian Portuguese\u0001pt-PT\u0001European Portuguese\u0001zh-Hans\u0001Simplified Chinese\u0001zh-Hant\u0001Traditional Chinese",
        "es" to
            "de-AT\u0001alemán austríaco\u0001de-CH\u0001alto alemán suizo\u0001en-AU\u0001inglés australiano\u0001en-CA\u0001inglés canadiense\u0001en-GB\u0001inglés británico\u0001en-US\u0001inglés estadounidense\u0001es-ES\u0001español de España\u0001es-MX\u0001español de México\u0001fr-CA\u0001francés canadiense\u0001fr-CH\u0001francés suizo\u0001nl-BE\u0001flamenco\u0001pt-BR\u0001portugués de Brasil\u0001pt-PT\u0001portugués de Portugal\u0001zh-Hans\u0001chino simplificado\u0001zh-Hant\u0001chino tradicional",
        "et" to
            "de-AT\u0001Austria saksa\u0001de-CH\u0001Šveitsi ülemsaksa\u0001en-AU\u0001Austraalia inglise\u0001en-CA\u0001Kanada inglise\u0001en-GB\u0001Briti inglise\u0001en-US\u0001Ameerika inglise\u0001es-ES\u0001Euroopa hispaania\u0001es-MX\u0001Mehhiko hispaania\u0001fr-CA\u0001Kanada prantsuse\u0001fr-CH\u0001Šveitsi prantsuse\u0001nl-BE\u0001flaami\u0001pt-BR\u0001Brasiilia portugali\u0001pt-PT\u0001Euroopa portugali\u0001zh-Hans\u0001lihtsustatud hiina\u0001zh-Hant\u0001traditsiooniline hiina",
        "eu" to
            "de-AT\u0001Austriako alemana\u0001de-CH\u0001Suitzako aleman garaia\u0001en-AU\u0001Australiako ingelesa\u0001en-CA\u0001Kanadako ingelesa\u0001en-GB\u0001Britainia Handiko ingelesa\u0001en-US\u0001ingeles amerikarra\u0001es-ES\u0001Europako gaztelania\u0001es-MX\u0001Mexikoko gaztelania\u0001fr-CA\u0001Kanadako frantsesa\u0001fr-CH\u0001Suitzako frantsesa\u0001nl-BE\u0001flandriera\u0001pt-BR\u0001Brasilgo portugesa\u0001pt-PT\u0001Europako portugesa\u0001zh-Hans\u0001txinera sinplifikatua\u0001zh-Hant\u0001txinera tradizionala",
        "fa" to
            "de-AT\u0001آلمانی اتریش\u0001de-CH\u0001آلمانی معیار سوئیس\u0001en-AU\u0001انگلیسی استرالیا\u0001en-CA\u0001انگلیسی کانادا\u0001en-GB\u0001انگلیسی بریتانیا\u0001en-US\u0001انگلیسی آمریکا\u0001es-ES\u0001اسپانیایی اروپا\u0001es-MX\u0001اسپانیایی مکزیک\u0001fr-CA\u0001فرانسوی کانادا\u0001fr-CH\u0001فرانسوی سوئیس\u0001nl-BE\u0001فلمنگی\u0001pt-BR\u0001پرتغالی برزیل\u0001pt-PT\u0001پرتغالی اروپا\u0001zh-Hans\u0001چینی ساده‌شده\u0001zh-Hant\u0001چینی سنتی",
        "fi" to
            "de-AT\u0001itävallansaksa\u0001de-CH\u0001sveitsinyläsaksa\u0001en-AU\u0001australianenglanti\u0001en-CA\u0001kanadanenglanti\u0001en-GB\u0001britannianenglanti\u0001en-US\u0001amerikanenglanti\u0001es-ES\u0001euroopanespanja\u0001es-MX\u0001meksikonespanja\u0001fr-CA\u0001kanadanranska\u0001fr-CH\u0001sveitsinranska\u0001nl-BE\u0001flaami\u0001pt-BR\u0001brasilianportugali\u0001pt-PT\u0001euroopanportugali",
        "fil" to
            "en-US\u0001Ingles (American)\u0001es-ES\u0001European Spanish\u0001es-MX\u0001Mexican na Espanyol\u0001nl-BE\u0001Flemish\u0001pt-PT\u0001European Portuguese\u0001zh-Hans\u0001Pinasimpleng Chinese\u0001zh-Hant\u0001Tradisyonal na Chinese",
        "fr" to
            "de-AT\u0001allemand autrichien\u0001de-CH\u0001allemand suisse\u0001en-AU\u0001anglais australien\u0001en-CA\u0001anglais canadien\u0001en-GB\u0001anglais britannique\u0001en-US\u0001anglais américain\u0001es-ES\u0001espagnol d’Espagne\u0001es-MX\u0001espagnol du Mexique\u0001fr-CA\u0001français canadien\u0001fr-CH\u0001français suisse\u0001nl-BE\u0001flamand\u0001pt-BR\u0001portugais brésilien\u0001pt-PT\u0001portugais européen\u0001zh-Hans\u0001chinois simplifié\u0001zh-Hant\u0001chinois traditionnel",
        "gl" to
            "de-AT\u0001alemán austríaco\u0001de-CH\u0001alto alemán suízo\u0001en-AU\u0001inglés australiano\u0001en-CA\u0001inglés canadense\u0001en-GB\u0001inglés británico\u0001en-US\u0001inglés estadounidense\u0001es-ES\u0001español de España\u0001es-MX\u0001español de México\u0001fr-CA\u0001francés canadense\u0001fr-CH\u0001francés suízo\u0001nl-BE\u0001flamengo\u0001pt-BR\u0001portugués do Brasil\u0001pt-PT\u0001portugués de Portugal\u0001zh-Hans\u0001chinés simplificado\u0001zh-Hant\u0001chinés tradicional",
        "gu" to
            "de-AT\u0001ઓસ્ટ્રિઅન જર્મન\u0001de-CH\u0001સ્વિસ હાય જર્મન\u0001en-AU\u0001ઓસ્ટ્રેલિયન અંગ્રેજી\u0001en-CA\u0001કેનેડિયન અંગ્રેજી\u0001en-GB\u0001બ્રિટિશ અંગ્રેજી\u0001en-US\u0001અમેરિકન અંગ્રેજી\u0001es-ES\u0001યુરોપિયન સ્પેનિશ\u0001es-MX\u0001મેક્સિકન સ્પેનિશ\u0001fr-CH\u0001સ્વિસ ફ્રેંચ\u0001nl-BE\u0001ફ્લેમિશ\u0001pt-BR\u0001બ્રાઝિલીયન પોર્ટુગીઝ\u0001pt-PT\u0001યુરોપિયન પોર્ટુગીઝ\u0001zh-Hans\u0001સરળીકૃત ચાઇનીઝ\u0001zh-Hant\u0001પારંપરિક ચાઇનીઝ",
        "ha" to
            "de-AT\u0001Jamusanci Ostiriya\u0001de-CH\u0001Jamusanci Suwizalan\u0001en-AU\u0001Turanci Ostareliya\u0001es-ES\u0001Sifaniyanci Turai\u0001es-MX\u0001Sifaniyanci Mesiko\u0001fr-CA\u0001Farasanci Kanada\u0001fr-CH\u0001Farasanci Suwizalan\u0001nl-BE\u0001Flemish\u0001pt-BR\u0001Harshen Potugis na Birazil\u0001pt-PT\u0001Potugis Ƙasashen Turai\u0001zh-Hans\u0001Sauƙaƙaƙƙen Sinanci\u0001zh-Hant\u0001Sinanci na gargajiya",
        "he" to
            "nl-BE\u0001הולנדית (פלמית)\u0001zh-Hans\u0001סינית פשוטה\u0001zh-Hant\u0001סינית מסורתית",
        "hi" to
            "de-AT\u0001ऑस्ट्रियाई जर्मन\u0001de-CH\u0001स्विस उच्च जर्मन\u0001en-AU\u0001ऑस्ट्रेलियाई अंग्रेज़ी\u0001en-CA\u0001कनाडाई अंग्रेज़ी\u0001en-GB\u0001ब्रिटिश अंग्रेज़ी\u0001en-US\u0001अमेरिकी अंग्रेज़ी\u0001es-ES\u0001यूरोपीय स्पेनिश\u0001es-MX\u0001मैक्सिकन स्पेनिश\u0001fr-CA\u0001कनाडाई फ़्रेंच\u0001fr-CH\u0001स्विस फ़्रेंच\u0001nl-BE\u0001फ़्लेमिश\u0001pt-BR\u0001ब्राज़ीली पुर्तगाली\u0001pt-PT\u0001यूरोपीय पुर्तगाली\u0001zh-Hans\u0001सरलीकृत चीनी\u0001zh-Hant\u0001पारंपरिक चीनी",
        "hr" to
            "de-AT\u0001austrijski njemački\u0001de-CH\u0001gornjonjemački (švicarski)\u0001en-AU\u0001australski engleski\u0001en-CA\u0001kanadski engleski\u0001en-GB\u0001britanski engleski\u0001en-US\u0001američki engleski\u0001es-ES\u0001europski španjolski\u0001es-MX\u0001meksički španjolski\u0001fr-CA\u0001kanadski francuski\u0001fr-CH\u0001švicarski francuski\u0001nl-BE\u0001flamanski\u0001pt-BR\u0001brazilski portugalski\u0001pt-PT\u0001europski portugalski\u0001zh-Hans\u0001kineski (pojednostavljeni)\u0001zh-Hant\u0001kineski (tradicionalni)",
        "hu" to
            "de-AT\u0001osztrák német\u0001de-CH\u0001svájci felnémet\u0001en-AU\u0001ausztrál angol\u0001en-CA\u0001kanadai angol\u0001en-GB\u0001brit angol\u0001en-US\u0001amerikai angol\u0001es-ES\u0001európai spanyol\u0001es-MX\u0001spanyol (mexikói)\u0001fr-CA\u0001kanadai francia\u0001fr-CH\u0001svájci francia\u0001nl-BE\u0001flamand\u0001pt-BR\u0001brazíliai portugál\u0001pt-PT\u0001európai portugál\u0001zh-Hans\u0001egyszerűsített kínai\u0001zh-Hant\u0001hagyományos kínai",
        "hy" to
            "de-AT\u0001ավստրիական գերմաներեն\u0001de-CH\u0001շվեյցարական վերին գերմաներեն\u0001en-AU\u0001ավստրալիական անգլերեն\u0001en-CA\u0001կանադական անգլերեն\u0001en-GB\u0001բրիտանական անգլերեն\u0001en-US\u0001ամերիկյան անգլերեն\u0001es-ES\u0001եվրոպական իսպաներեն\u0001es-MX\u0001մեքսիկական իսպաներեն\u0001fr-CA\u0001կանադական ֆրանսերեն\u0001fr-CH\u0001շվեյցարական ֆրանսերեն\u0001nl-BE\u0001ֆլամանդերեն\u0001pt-BR\u0001բրազիլական պորտուգալերեն\u0001pt-PT\u0001եվրոպական պորտուգալերեն\u0001zh-Hans\u0001պարզեցված չինարեն\u0001zh-Hant\u0001ավանդական չինարեն",
        "id" to
            "de-CH\u0001Jerman Tinggi (Swiss)\u0001en-GB\u0001Inggris (Britania)\u0001es-ES\u0001Spanyol (Eropa)\u0001pt-PT\u0001Portugis (Eropa)",
        "ig" to
            "de-AT\u0001Austrian German\u0001de-CH\u0001Swiss High German\u0001en-AU\u0001Bekee ndị Australia\u0001en-CA\u0001Bekee ndị Canada\u0001en-GB\u0001Bekee ndị United Kingdom\u0001en-US\u0001Bekee ndị America\u0001es-ES\u0001Spanish ndị Europe\u0001es-MX\u0001Spanish ndị Mexico\u0001fr-CA\u0001Canadian French\u0001fr-CH\u0001Swiss French\u0001nl-BE\u0001Flemish\u0001pt-BR\u0001Asụsụ Portugese ndị Brazil\u0001pt-PT\u0001Asụsụ Portuguese ndị Europe\u0001zh-Hans\u0001Asụsụ Chaịniiz dị mfe\u0001zh-Hant\u0001Asụsụ ọdịnala Chaịniiz",
        "is" to
            "de-AT\u0001austurrísk þýska\u0001de-CH\u0001svissnesk háþýska\u0001en-AU\u0001áströlsk enska\u0001en-CA\u0001kanadísk enska\u0001en-GB\u0001bresk enska\u0001en-US\u0001bandarísk enska\u0001es-ES\u0001evrópsk spænska\u0001es-MX\u0001mexíkósk spænska\u0001fr-CA\u0001kanadísk franska\u0001fr-CH\u0001svissnesk franska\u0001nl-BE\u0001flæmska\u0001pt-BR\u0001brasílísk portúgalska\u0001pt-PT\u0001evrópsk portúgalska\u0001zh-Hans\u0001kínverska (einfölduð)\u0001zh-Hant\u0001kínverska (hefðbundin)",
        "it" to
            "de-AT\u0001tedesco austriaco\u0001de-CH\u0001alto tedesco svizzero\u0001en-AU\u0001inglese australiano\u0001en-CA\u0001inglese canadese\u0001en-GB\u0001inglese britannico\u0001en-US\u0001inglese americano\u0001es-ES\u0001spagnolo europeo\u0001es-MX\u0001spagnolo messicano\u0001fr-CA\u0001francese canadese\u0001fr-CH\u0001francese svizzero\u0001nl-BE\u0001fiammingo\u0001pt-BR\u0001portoghese brasiliano\u0001pt-PT\u0001portoghese europeo\u0001zh-Hans\u0001cinese semplificato\u0001zh-Hant\u0001cinese tradizionale",
        "ja" to
            "de-CH\u0001標準ドイツ語 (スイス)\u0001en-AU\u0001オーストラリア英語\u0001en-CA\u0001カナダ英語\u0001en-GB\u0001イギリス英語\u0001en-US\u0001アメリカ英語\u0001es-ES\u0001スペイン語 (イベリア半島)\u0001nl-BE\u0001フラマン語\u0001pt-PT\u0001ポルトガル語 (イベリア半島)\u0001zh-Hans\u0001簡体中国語\u0001zh-Hant\u0001繁体中国語",
        "jv" to
            "de-AT\u0001Jérman Ostenrik\u0001de-CH\u0001Jérman Switserlan\u0001en-AU\u0001Inggris Ostrali\u0001en-CA\u0001Inggris Kanada\u0001en-GB\u0001Inggris Karajan Manunggal\u0001en-US\u0001Inggris Amérika Sarékat\u0001es-ES\u0001Spanyol (Eropah)\u0001es-MX\u0001Spanyol (Meksiko)\u0001fr-CA\u0001Prancis Kanada\u0001fr-CH\u0001Prancis Switserlan\u0001nl-BE\u0001Flemis\u0001pt-BR\u0001Portugis Brasil\u0001pt-PT\u0001Portugis Portugal\u0001zh-Hans\u0001Tyonghwa (Ringkes)\u0001zh-Hant\u0001Tyonghwa (Tradisional)",
        "ka" to
            "de-AT\u0001ავსტრიული გერმანული\u0001de-CH\u0001შვეიცარიული ზემოგერმანული\u0001en-AU\u0001ავსტრალიური ინგლისური\u0001en-CA\u0001კანადური ინგლისური\u0001en-GB\u0001ბრიტანული ინგლისური\u0001en-US\u0001ამერიკული ინგლისური\u0001es-ES\u0001ევროპული ესპანური\u0001es-MX\u0001მექსიკური ესპანური\u0001fr-CA\u0001კანადური ფრანგული\u0001fr-CH\u0001შვეიცარიული ფრანგული\u0001nl-BE\u0001ფლამანდიური\u0001pt-BR\u0001ბრაზილიური პორტუგალიური\u0001pt-PT\u0001ევროპული პორტუგალიური\u0001zh-Hans\u0001გამარტივებული ჩინური\u0001zh-Hant\u0001ტრადიციული ჩინური",
        "kk" to
            "de-CH\u0001швейцариялық әдеби неміс тілі\u0001nl-BE\u0001фламанд тілі\u0001pt-BR\u0001бразилиялық португал тілі\u0001pt-PT\u0001еуропалық португал тілі\u0001zh-Hans\u0001жеңілдетілген қытай тілі\u0001zh-Hant\u0001дәстүрлі қытай тілі",
        "km" to
            "es-ES\u0001អេស្ប៉ាញ (អ៊ឺរ៉ុប)\u0001nl-BE\u0001ផ្លាមីស\u0001pt-BR\u0001ព័រទុយហ្កាល់ (ប្រេស៊ីល)\u0001pt-PT\u0001ព័រទុយហ្គាល់ (អឺរ៉ុប)\u0001zh-Hans\u0001ចិន​អក្សរ​កាត់\u0001zh-Hant\u0001ចិន​អក្សរ​ពេញ",
        "kn" to
            "de-AT\u0001ಆಸ್ಟ್ರಿಯನ್ ಜರ್ಮನ್\u0001de-CH\u0001ಸ್ವಿಸ್ ಹೈ ಜರ್ಮನ್\u0001en-AU\u0001ಆಸ್ಟ್ರೇಲಿಯನ್ ಇಂಗ್ಲಿಷ್\u0001en-CA\u0001ಕೆನೆಡಿಯನ್ ಇಂಗ್ಲಿಷ್\u0001en-GB\u0001ಬ್ರಿಟಿಷ್ ಇಂಗ್ಲಿಷ್\u0001en-US\u0001ಅಮೆರಿಕನ್ ಇಂಗ್ಲಿಷ್\u0001es-ES\u0001ಯುರೋಪಿಯನ್ ಸ್ಪ್ಯಾನಿಷ್\u0001es-MX\u0001ಮೆಕ್ಸಿಕನ್ ಸ್ಪ್ಯಾನಿಷ್\u0001fr-CA\u0001ಕೆನೆಡಿಯನ್ ಫ್ರೆಂಚ್\u0001fr-CH\u0001ಸ್ವಿಸ್ ಫ್ರೆಂಚ್\u0001nl-BE\u0001ಫ್ಲೆಮಿಷ್\u0001pt-BR\u0001ಬ್ರೆಜಿಲಿಯನ್ ಪೋರ್ಚುಗೀಸ್\u0001pt-PT\u0001ಯೂರೋಪಿಯನ್ ಪೋರ್ಚುಗೀಸ್\u0001zh-Hans\u0001ಸರಳೀಕೃತ ಚೈನೀಸ್\u0001zh-Hant\u0001ಸಾಂಪ್ರದಾಯಿಕ ಚೈನೀಸ್",
        "ko" to
            "de-CH\u0001고지 독일어(스위스)\u0001es-ES\u0001스페인어(유럽)\u0001nl-BE\u0001플라망어\u0001pt-PT\u0001포르투갈어(유럽)",
        "ku" to
            "de-CH\u0001almanîya bilind a swîsreyî\u0001en-GB\u0001îngilîzî (brîtanî)\u0001en-US\u0001îngilîzî (amerîkî)\u0001es-ES\u0001spanî (Ewropa)\u0001nl-BE\u0001flamî\u0001pt-PT\u0001portugalî (Ewropa)\u0001zh-Hans\u0001çînîya sadekirî\u0001zh-Hant\u0001çînîya edetî",
        "ky" to
            "de-CH\u0001адабий немисче (Швейцария)\u0001en-US\u0001англисче (Америка Кошмо Штаттары)\u0001es-ES\u0001испанча (Европа)\u0001nl-BE\u0001фламандча\u0001pt-PT\u0001португалча (Европа)\u0001zh-Hans\u0001кытайча (жөнөкөйлөштүрүлгөн)\u0001zh-Hant\u0001кытайча (салттуу)",
        "lo" to
            "de-AT\u0001ເຢຍລະມັນ (ໂອສຕຣິດ)\u0001de-CH\u0001ສະວິສ ໄຮ ເຈີແມນ\u0001en-GB\u0001ອັງກິດ (ບຣິດທິຊ)\u0001en-US\u0001ອັງກິດ (ອາເມລິກັນ)\u0001es-ES\u0001ສະເປັນ ຢຸໂຣບ\u0001es-MX\u0001ເມັກຊິກັນ ສະແປນນິຊ\u0001fr-CH\u0001ຝຣັ່ງ (ສວິສ)\u0001nl-BE\u0001ຟລີມິຊ\u0001pt-PT\u0001ປອກຕຸຍກິສ ຢຸໂຣບ\u0001zh-Hans\u0001ຈີນແບບຮຽບງ່າຍ\u0001zh-Hant\u0001ຈີນແບບດັ້ງເດີມ",
        "lt" to
            "de-AT\u0001Austrijos vokiečių\u0001de-CH\u0001Šveicarijos aukštutinė vokiečių\u0001en-AU\u0001Australijos anglų\u0001en-CA\u0001Kanados anglų\u0001en-GB\u0001Didžiosios Britanijos anglų\u0001en-US\u0001Jungtinių Valstijų anglų\u0001fr-CA\u0001Kanados prancūzų\u0001fr-CH\u0001Šveicarijos prancūzų\u0001nl-BE\u0001flamandų\u0001zh-Hans\u0001supaprastintoji kinų\u0001zh-Hant\u0001tradicinė kinų",
        "lv" to
            "de-CH\u0001augšvācu (Šveice)\u0001en-GB\u0001angļu (Lielbritānija)\u0001nl-BE\u0001flāmu\u0001zh-Hans\u0001ķīniešu vienkāršotā\u0001zh-Hant\u0001ķīniešu tradicionālā",
        "mk" to
            "de-AT\u0001австриски германски\u0001de-CH\u0001швајцарски горногермански\u0001en-AU\u0001австралиски англиски\u0001en-CA\u0001канадски англиски\u0001en-GB\u0001британски англиски\u0001en-US\u0001американски англиски\u0001es-ES\u0001шпански (Европа)\u0001es-MX\u0001мексикански шпански\u0001fr-CA\u0001канадски француски\u0001fr-CH\u0001швајцарски француски\u0001nl-BE\u0001фламански\u0001pt-BR\u0001бразилски португалски\u0001pt-PT\u0001португалски (Европа)\u0001zh-Hans\u0001поедноставен кинески\u0001zh-Hant\u0001традиционален кинески",
        "ml" to
            "de-AT\u0001ഓസ്‌ട്രിയൻ ജർമ്മൻ\u0001de-CH\u0001സ്വിസ് ഹൈ ജർമ്മൻ\u0001en-AU\u0001ഓസ്‌ട്രേലിയൻ ഇംഗ്ലീഷ്\u0001en-CA\u0001കനേഡിയൻ ഇംഗ്ലീഷ്\u0001en-GB\u0001ബ്രിട്ടീഷ് ഇംഗ്ലീഷ്\u0001en-US\u0001അമേരിക്കൻ ഇംഗ്ലീഷ്\u0001es-ES\u0001യൂറോപ്യൻ സ്‌പാനിഷ്\u0001es-MX\u0001മെക്സിക്കൻ സ്പാനിഷ്\u0001fr-CA\u0001കനേഡിയൻ ഫ്രഞ്ച്\u0001fr-CH\u0001സ്വിസ് ഫ്രഞ്ച്\u0001nl-BE\u0001ഫ്ലമിഷ്\u0001pt-BR\u0001ബ്രസീലിയൻ പോർച്ചുഗീസ്\u0001pt-PT\u0001യൂറോപ്യൻ പോർച്ചുഗീസ്\u0001zh-Hans\u0001ലളിതമാക്കിയ ചൈനീസ്\u0001zh-Hant\u0001പരമ്പരാഗത ചൈനീസ്",
        "mn" to
            "en-AU\u0001австрали-англи\u0001en-CA\u0001канад-англи\u0001en-GB\u0001британи-англи\u0001en-US\u0001америк-англи\u0001es-ES\u0001испани хэл (Европ)\u0001es-MX\u0001испани хэл (Мексик)\u0001nl-BE\u0001фламанд\u0001pt-BR\u0001португал хэл (Бразил)\u0001pt-PT\u0001португал хэл (Европ)\u0001zh-Hans\u0001хялбаршуулсан хятад\u0001zh-Hant\u0001уламжлалт хятад",
        "mr" to
            "de-AT\u0001ऑस्ट्रियन जर्मन\u0001de-CH\u0001स्विस हाय जर्मन\u0001en-AU\u0001ऑस्ट्रेलियन इंग्रजी\u0001en-CA\u0001कॅनडियन इंग्रजी\u0001en-GB\u0001ब्रिटिश इंग्रजी\u0001en-US\u0001अमेरिकन इंग्रजी\u0001es-ES\u0001युरोपियन स्पॅनिश\u0001es-MX\u0001मेक्सिकन स्पॅनिश\u0001fr-CA\u0001कॅनडियन फ्रेंच\u0001fr-CH\u0001स्विस फ्रेंच\u0001nl-BE\u0001फ्लेमिश\u0001pt-BR\u0001ब्राझिलियन पोर्तुगीज\u0001pt-PT\u0001युरोपियन पोर्तुगीज\u0001zh-Hans\u0001सरलीकृत चीनी\u0001zh-Hant\u0001पारंपारिक चीनी",
        "ms" to
            "de-AT\u0001Jerman Austria\u0001de-CH\u0001Jerman Halus Switzerland\u0001en-AU\u0001Inggeris Australia\u0001en-CA\u0001Inggeris Kanada\u0001en-GB\u0001Inggeris British\u0001en-US\u0001Inggeris AS\u0001es-ES\u0001Sepanyol Eropah\u0001es-MX\u0001Sepanyol Mexico\u0001fr-CA\u0001Perancis Kanada\u0001fr-CH\u0001Perancis Switzerland\u0001nl-BE\u0001Flemish\u0001pt-BR\u0001Portugis Brazil\u0001pt-PT\u0001Portugis Eropah\u0001zh-Hans\u0001Cina Ringkas\u0001zh-Hant\u0001Cina Tradisional",
        "my" to
            "de-AT\u0001ဩစတြီးယား ဂျာမန်\u0001de-CH\u0001အလီမဲန်နစ် ဂျာမန်\u0001en-AU\u0001ဩစတြေးလျ အင်္ဂလိပ်\u0001en-CA\u0001ကနေဒါ အင်္ဂလိပ်\u0001en-GB\u0001ဗြိတိသျှ အင်္ဂလိပ်\u0001en-US\u0001အမေရိကန် အင်္ဂလိပ်\u0001es-ES\u0001စပိန် (ဥရောပ)\u0001es-MX\u0001စပိန် (မက္ကဆီကို)\u0001fr-CA\u0001ကနေဒါ ပြင်သစ်\u0001fr-CH\u0001ဆွစ် ပြင်သစ်\u0001nl-BE\u0001ဖလီမစ်ရှ်\u0001pt-BR\u0001ဘရာဇီး ပေါ်တူဂီ\u0001pt-PT\u0001ဥရောပ ပေါ်တူဂီ",
        "nb" to
            "nl-BE\u0001flamsk\u0001zh-Hans\u0001forenklet kinesisk\u0001zh-Hant\u0001tradisjonell kinesisk",
        "ne" to
            "de-AT\u0001अस्ट्रिएन जर्मन\u0001de-CH\u0001स्वीस हाई जर्मन\u0001en-AU\u0001अस्ट्रेलियाली अङ्ग्रेजी\u0001en-CA\u0001क्यानाडेली अङ्ग्रेजी\u0001en-GB\u0001बेलायती अङ्ग्रेजी\u0001en-US\u0001अमेरिकी अङ्ग्रेजी\u0001es-ES\u0001युरोपेली स्पेनी\u0001es-MX\u0001मेक्सिकन स्पेनी\u0001fr-CA\u0001क्यानेडाली फ्रान्सेली\u0001fr-CH\u0001स्विस फ्रेन्च\u0001nl-BE\u0001फ्लेमिस\u0001pt-BR\u0001ब्राजिली पोर्तुगी\u0001pt-PT\u0001युरोपेली पोर्तुगी\u0001zh-Hans\u0001सरलिकृत चिनियाँ\u0001zh-Hant\u0001परम्परागत चिनियाँ",
        "nl" to
            "nl-BE\u0001Vlaams",
        "nn" to
            "nl-BE\u0001flamsk\u0001zh-Hans\u0001forenkla kinesisk\u0001zh-Hant\u0001tradisjonell kinesisk",
        "no" to
            "nl-BE\u0001flamsk\u0001zh-Hans\u0001forenklet kinesisk\u0001zh-Hant\u0001tradisjonell kinesisk",
        "or" to
            "de-AT\u0001ଅଷ୍ଟ୍ରିଆନ୍ ଜର୍ମାନ\u0001de-CH\u0001ସ୍ୱିସ୍‌ ହାଇ ଜର୍ମାନ\u0001en-AU\u0001ଅଷ୍ଟ୍ରେଲିୟ ଇଂରାଜୀ\u0001en-CA\u0001କାନାଡିୟ ଇଂରାଜୀ\u0001en-GB\u0001ବ୍ରିଟିଶ୍‌ ଇଂରାଜୀ\u0001en-US\u0001ଆମେରିକୀୟ ଇଂରାଜୀ\u0001es-ES\u0001ୟୁରୋପୀୟ ସ୍ପାନିସ୍‌\u0001es-MX\u0001ମେକ୍ସିକାନ ସ୍ପାନିସ୍‌\u0001fr-CA\u0001କାନାଡିୟ ଫ୍ରେଞ୍ଚ\u0001fr-CH\u0001ସ୍ୱିସ୍ ଫ୍ରେଞ୍ଚ\u0001nl-BE\u0001ଫ୍ଲେମିଶ୍\u0001pt-BR\u0001ବ୍ରାଜିଲିଆନ୍ ପର୍ତ୍ତୁଗୀଜ୍\u0001pt-PT\u0001ୟୁରୋପୀୟ ପର୍ତ୍ତୁଗୀଜ୍‌\u0001zh-Hans\u0001ସରଳୀକୃତ ଚାଇନିଜ\u0001zh-Hant\u0001ପାରମ୍ପରିକ ଚାଇନିଜ୍‌",
        "pa" to
            "de-AT\u0001ਜਰਮਨ (ਆਸਟਰੀਆਈ)\u0001de-CH\u0001ਹਾਈ ਜਰਮਨ (ਸਵਿਟਜ਼ਰਲੈਂਡ)\u0001en-GB\u0001ਅੰਗਰੇਜ਼ੀ (ਬਰਤਾਨਵੀ)\u0001en-US\u0001ਅੰਗਰੇਜ਼ੀ (ਅਮਰੀਕੀ)\u0001es-ES\u0001ਸਪੇਨੀ (ਯੂਰਪੀ)\u0001fr-CA\u0001ਫਰਾਂਸੀਸੀ (ਕੈਨੇਡੀਅਨ)\u0001nl-BE\u0001ਫਲੈਮਿਸ਼\u0001pt-BR\u0001ਪੁਰਤਗਾਲੀ (ਬ੍ਰਾਜ਼ੀਲੀ)\u0001pt-PT\u0001ਪੁਰਤਗਾਲੀ (ਯੂਰਪੀ)",
        "pl" to
            "de-AT\u0001niemiecki austriacki\u0001de-CH\u0001wysokoniemiecki szwajcarski\u0001en-AU\u0001angielski australijski\u0001en-CA\u0001angielski kanadyjski\u0001en-GB\u0001angielski brytyjski\u0001en-US\u0001angielski amerykański\u0001es-ES\u0001europejski hiszpański\u0001es-MX\u0001meksykański hiszpański\u0001fr-CA\u0001francuski kanadyjski\u0001fr-CH\u0001francuski szwajcarski\u0001nl-BE\u0001flamandzki\u0001pt-BR\u0001brazylijski portugalski\u0001pt-PT\u0001europejski portugalski\u0001zh-Hans\u0001chiński uproszczony\u0001zh-Hant\u0001chiński tradycyjny",
        "ps" to
            "de-AT\u0001اتريشي آلماني\u0001de-CH\u0001سویس های جرمن\u0001en-AU\u0001آسټرالياوي انګليسي\u0001en-CA\u0001کاناډايي انګلیسي\u0001en-GB\u0001بريتانوی انګلیسي\u0001es-ES\u0001اروپايي هسپانوي\u0001es-MX\u0001ميکسيکي هسپانوي\u0001fr-CA\u0001کاناډايي فرانسوي\u0001fr-CH\u0001سويسي فرانسوي\u0001nl-BE\u0001فلېمېشي\u0001pt-BR\u0001برازیلي پرتګالي\u0001pt-PT\u0001اروپايي پرتګالي\u0001zh-Hant\u0001دوديزه چيني",
        "pt" to
            "de-CH\u0001alto alemão (Suíça)\u0001nl-BE\u0001flamengo\u0001zh-Hans\u0001chinês simplificado\u0001zh-Hant\u0001chinês tradicional",
        "ro" to
            "de-CH\u0001germană standard (Elveția)\u0001es-ES\u0001spaniolă (Europa)\u0001nl-BE\u0001flamandă\u0001pt-PT\u0001portugheză (Europa)\u0001zh-Hans\u0001chineză simplificată\u0001zh-Hant\u0001chineză tradițională",
        "ru" to
            "de-AT\u0001австрийский немецкий\u0001de-CH\u0001литературный швейцарский немецкий\u0001en-AU\u0001австралийский английский\u0001en-CA\u0001канадский английский\u0001en-GB\u0001британский английский\u0001en-US\u0001американский английский\u0001es-ES\u0001европейский испанский\u0001es-MX\u0001мексиканский испанский\u0001fr-CA\u0001канадский французский\u0001fr-CH\u0001швейцарский французский\u0001nl-BE\u0001фламандский\u0001pt-BR\u0001бразильский португальский\u0001pt-PT\u0001европейский португальский\u0001zh-Hans\u0001китайский, упрощенное письмо\u0001zh-Hant\u0001китайский, традиционное письмо",
        "sd" to
            "de-AT\u0001آسٽريائي جرمن\u0001de-CH\u0001سوئس هائي جرمن\u0001en-AU\u0001آسٽريليائي انگريزي\u0001en-CA\u0001ڪينيڊيائي انگريزي\u0001en-GB\u0001برطانوي انگريزي\u0001en-US\u0001آمريڪي انگريزي\u0001es-ES\u0001يورپي اسپيني\u0001es-MX\u0001ميڪسيڪين اسپيني\u0001fr-CA\u0001ڪينيڊيائي فرانسيسي\u0001fr-CH\u0001سوئس فرانسيسي\u0001nl-BE\u0001فليمش\u0001pt-BR\u0001برازيلي پرتگالي\u0001pt-PT\u0001يورپي پرتگالي\u0001zh-Hant\u0001روايتي چيني",
        "si" to
            "de-AT\u0001ඔස්ට්‍රියානු ජර්මන්\u0001de-CH\u0001ස්විස් උසස් ජර්මන්\u0001en-AU\u0001ඕස්ට්‍රේලියානු ඉංග්‍රීසි\u0001en-CA\u0001කැනේඩියානු ඉංග්‍රීසි\u0001en-GB\u0001බ්‍රිතාන්‍ය ඉංග්‍රීසි\u0001en-US\u0001ඇමෙරිකානු ඉංග්‍රීසි\u0001es-ES\u0001යුරෝපීය ස්පාඤ්ඤ\u0001es-MX\u0001මෙක්සිකානු ස්පාඤ්ඤ\u0001fr-CA\u0001කැනේඩියානු ප්‍රංශ\u0001fr-CH\u0001ස්විස් ප්‍රංශ\u0001nl-BE\u0001ෆ්ලෙමිශ්\u0001pt-BR\u0001බ්‍රසීල පෘතුගීසි\u0001pt-PT\u0001යුරෝපීය පෘතුගීසි\u0001zh-Hans\u0001සරල චීන\u0001zh-Hant\u0001සාම්ප්‍රදායික චීන",
        "sk" to
            "de-AT\u0001nemčina (rakúska)\u0001de-CH\u0001nemčina (švajčiarska spisovná)\u0001en-AU\u0001angličtina (austrálska)\u0001en-CA\u0001angličtina (kanadská)\u0001en-GB\u0001angličtina (britská)\u0001en-US\u0001angličtina (americká)\u0001es-ES\u0001španielčina (európska)\u0001es-MX\u0001španielčina (mexická)\u0001fr-CA\u0001francúzština (kanadská)\u0001fr-CH\u0001francúzština (švajčiarska)\u0001nl-BE\u0001flámčina\u0001pt-BR\u0001portugalčina (brazílska)\u0001pt-PT\u0001portugalčina (európska)\u0001zh-Hans\u0001čínština (zjednodušená)\u0001zh-Hant\u0001čínština (tradičná)",
        "sl" to
            "de-AT\u0001avstrijska nemščina\u0001de-CH\u0001visoka nemščina (Švica)\u0001en-AU\u0001avstralska angleščina\u0001en-CA\u0001kanadska angleščina\u0001en-GB\u0001britanska angleščina\u0001en-US\u0001ameriška angleščina\u0001es-ES\u0001evropska španščina\u0001es-MX\u0001mehiška španščina\u0001fr-CA\u0001kanadska francoščina\u0001fr-CH\u0001švicarska francoščina\u0001nl-BE\u0001flamščina\u0001pt-BR\u0001brazilska portugalščina\u0001pt-PT\u0001evropska portugalščina\u0001zh-Hans\u0001poenostavljena kitajščina\u0001zh-Hant\u0001tradicionalna kitajščina",
        "so" to
            "de-AT\u0001Jarmal Awsteeriya\u0001de-CH\u0001Iswiiska Sare ee Jarmal\u0001en-AU\u0001Ingiriis Austaraaliyaan\u0001en-CA\u0001Ingiriis Kanadiyaan\u0001en-GB\u0001Ingiriis Biritish\u0001en-US\u0001Ingiriis Maraykan\u0001es-ES\u0001Isbaanish Yurub\u0001es-MX\u0001Isbaanishka Mexico\u0001fr-CA\u0001Faransiiska Kanada\u0001fr-CH\u0001Faransiis (Iswiiserlaand)\u0001nl-BE\u0001Af faleemi\u0001pt-BR\u0001Boortaqiiska Baraasiil\u0001pt-PT\u0001Boortaqiiska Yurub\u0001zh-Hans\u0001Shiinaha Rasmiga ah\u0001zh-Hant\u0001Af-Shiineeska Qadiimiga ah",
        "sq" to
            "de-AT\u0001gjermanishte austriake\u0001de-CH\u0001gjermanishte zvicerane (dialekti i Alpeve)\u0001en-AU\u0001anglishte australiane\u0001en-CA\u0001anglishte kanadeze\u0001en-GB\u0001anglishte britanike\u0001en-US\u0001anglishte amerikane\u0001es-ES\u0001spanjishte evropiane\u0001es-MX\u0001spanjishte meksikane\u0001fr-CA\u0001frëngjishte kanadeze\u0001fr-CH\u0001frëngjishte zvicerane\u0001nl-BE\u0001flamandisht\u0001pt-BR\u0001portugalishte braziliane\u0001pt-PT\u0001portugalishte evropiane\u0001zh-Hans\u0001kinezishte e thjeshtuar\u0001zh-Hant\u0001kinezishte tradicionale",
        "sr" to
            "de-AT\u0001аустријски немачки\u0001de-CH\u0001швајцарски високи немачки\u0001en-GB\u0001енглески (Велика Британија)\u0001en-US\u0001енглески (Сједињене Америчке Државе)\u0001es-ES\u0001европски шпански\u0001es-MX\u0001мексички шпански\u0001fr-CA\u0001канадски француски\u0001fr-CH\u0001швајцарски француски\u0001nl-BE\u0001фламански\u0001pt-BR\u0001бразилски португалски\u0001pt-PT\u0001европски португалски\u0001zh-Hans\u0001поједностављени кинески\u0001zh-Hant\u0001традиционални кинески",
        "su" to
            "de-AT\u0001Jérman Austria\u0001de-CH\u0001Jérman Swiss Luhur\u0001en-AU\u0001Inggris Australia\u0001en-CA\u0001Inggris Kanada\u0001en-US\u0001Inggris Amerika\u0001es-ES\u0001Spanyol Éropa\u0001es-MX\u0001Spanyol Méksiko\u0001fr-CA\u0001Prancis Kanada\u0001fr-CH\u0001Prancis Swiss\u0001pt-BR\u0001Portugis Brasil\u0001pt-PT\u0001Portugis Éropa\u0001zh-Hans\u0001Tiongkok Sederhana\u0001zh-Hant\u0001Tiongkok Tradisional",
        "sv" to
            "de-AT\u0001österrikisk tyska\u0001de-CH\u0001schweizisk högtyska\u0001en-AU\u0001australisk engelska\u0001en-CA\u0001kanadensisk engelska\u0001en-GB\u0001brittisk engelska\u0001en-US\u0001amerikansk engelska\u0001es-ES\u0001europeisk spanska\u0001es-MX\u0001mexikansk spanska\u0001fr-CA\u0001kanadensisk franska\u0001fr-CH\u0001schweizisk franska\u0001nl-BE\u0001flamländska\u0001pt-BR\u0001brasiliansk portugisiska\u0001pt-PT\u0001europeisk portugisiska\u0001zh-Hans\u0001förenklad kinesiska\u0001zh-Hant\u0001traditionell kinesiska",
        "sw" to
            "en-GB\u0001Kiingereza (Uingereza)\u0001es-ES\u0001Kihispania (Ulaya)\u0001nl-BE\u0001Kiflemi\u0001pt-BR\u0001Kireno (Brazili)\u0001pt-PT\u0001Kireno (Ulaya)\u0001zh-Hans\u0001Kichina (Kilichorahisishwa)\u0001zh-Hant\u0001Kichina cha Jadi",
        "ta" to
            "de-AT\u0001ஆஸ்திரிய ஜெர்மன்\u0001de-CH\u0001ஸ்விஸ் ஹை ஜெர்மன்\u0001en-AU\u0001ஆஸ்திரேலிய ஆங்கிலம்\u0001en-CA\u0001கனடிய ஆங்கிலம்\u0001en-GB\u0001பிரிட்டிஷ் ஆங்கிலம்\u0001en-US\u0001அமெரிக்க ஆங்கிலம்\u0001es-ES\u0001ஐரோப்பிய ஸ்பானிஷ்\u0001es-MX\u0001மெக்ஸிகன் ஸ்பானிஷ்\u0001fr-CA\u0001கனடிய பிரெஞ்சு\u0001fr-CH\u0001ஸ்விஸ் பிரஞ்சு\u0001nl-BE\u0001ஃப்லெமிஷ்\u0001pt-BR\u0001பிரேசிலிய போர்ச்சுகீஸ்\u0001pt-PT\u0001ஐரோப்பிய போர்ச்சுகீஸ்\u0001zh-Hans\u0001எளிதாக்கப்பட்ட சீனம்\u0001zh-Hant\u0001பாரம்பரிய சீனம்",
        "te" to
            "de-AT\u0001ఆస్ట్రియన్ జర్మన్\u0001de-CH\u0001స్విస్ హై జర్మన్\u0001en-AU\u0001ఆస్ట్రేలియన్ ఇంగ్లీష్\u0001en-CA\u0001కెనడియన్ ఇంగ్లీష్\u0001en-GB\u0001బ్రిటిష్ ఇంగ్లీష్\u0001en-US\u0001అమెరికన్ ఇంగ్లీష్\u0001es-ES\u0001యూరోపియన్ స్పానిష్\u0001es-MX\u0001మెక్సికన్ స్పానిష్\u0001fr-CA\u0001కెనడియెన్ ఫ్రెంచ్\u0001fr-CH\u0001స్విస్ ఫ్రెంచ్\u0001nl-BE\u0001ఫ్లెమిష్\u0001pt-BR\u0001బ్రెజీలియన్ పోర్చుగీస్\u0001pt-PT\u0001యూరోపియన్ పోర్చుగీస్\u0001zh-Hans\u0001సరళీకృత చైనీస్\u0001zh-Hant\u0001సాంప్రదాయక చైనీస్",
        "tg" to
            "de-AT\u0001немисии австриягӣ\u0001de-CH\u0001немисии швейсарии болоӣ\u0001en-AU\u0001англисии австралиягӣ\u0001en-CA\u0001англисии канадагӣ\u0001en-GB\u0001англисии британӣ\u0001en-US\u0001англисии америкоӣ\u0001es-ES\u0001испании аврупоӣ\u0001es-MX\u0001испании мексикоӣ\u0001fr-CA\u0001франсузии канадагӣ\u0001fr-CH\u0001франсузии швейсарӣ\u0001nl-BE\u0001Фламандӣ\u0001pt-BR\u0001португалии бразилиягӣ\u0001pt-PT\u0001португалии аврупоӣ\u0001zh-Hans\u0001хитоии осонфаҳм\u0001zh-Hant\u0001хитоии анъанавӣ",
        "th" to
            "de-AT\u0001เยอรมัน - ออสเตรีย\u0001de-CH\u0001เยอรมันสูง (สวิส)\u0001en-AU\u0001อังกฤษ - ออสเตรเลีย\u0001en-CA\u0001อังกฤษ - แคนาดา\u0001en-GB\u0001อังกฤษ - สหราชอาณาจักร\u0001en-US\u0001อังกฤษ - สหรัฐอเมริกา\u0001es-ES\u0001สเปน - ยุโรป\u0001es-MX\u0001สเปน - เม็กซิโก\u0001fr-CA\u0001ฝรั่งเศส - แคนาดา\u0001fr-CH\u0001ฝรั่งเศส - สวิส\u0001nl-BE\u0001เฟลมิช\u0001pt-BR\u0001โปรตุเกส - บราซิล\u0001pt-PT\u0001โปรตุเกส - ยุโรป\u0001zh-Hans\u0001จีนตัวย่อ\u0001zh-Hant\u0001จีนตัวเต็ม",
        "tl" to
            "en-US\u0001Ingles (American)\u0001es-ES\u0001European Spanish\u0001es-MX\u0001Mexican na Espanyol\u0001nl-BE\u0001Flemish\u0001pt-PT\u0001European Portuguese\u0001zh-Hans\u0001Pinasimpleng Chinese\u0001zh-Hant\u0001Tradisyonal na Chinese",
        "tr" to
            "de-AT\u0001Avusturya Almancası\u0001de-CH\u0001İsviçre Yüksek Almancası\u0001en-AU\u0001Avustralya İngilizcesi\u0001en-CA\u0001Kanada İngilizcesi\u0001en-GB\u0001İngiliz İngilizcesi\u0001en-US\u0001Amerikan İngilizcesi\u0001es-ES\u0001Avrupa İspanyolcası\u0001es-MX\u0001Meksika İspanyolcası\u0001fr-CA\u0001Kanada Fransızcası\u0001fr-CH\u0001İsviçre Fransızcası\u0001nl-BE\u0001Flamanca\u0001pt-BR\u0001Brezilya Portekizcesi\u0001pt-PT\u0001Avrupa Portekizcesi\u0001zh-Hans\u0001Basitleştirilmiş Çince\u0001zh-Hant\u0001Geleneksel Çince",
        "ug" to
            "de-AT\u0001ئاۋستىرىيە گېرمانچە\u0001de-CH\u0001شىۋىتسارىيە ئېگىزلىك گېرمانچە\u0001en-AU\u0001ئاۋسترالىيە ئىنگلىزچە\u0001en-CA\u0001كانادا ئىنگلىزچە\u0001en-GB\u0001ئەنگلىيە ئىنگلىزچە\u0001en-US\u0001ئامېرىكا ئىنگلىزچە\u0001es-ES\u0001ياۋروپا ئىسپانچە\u0001es-MX\u0001مېكسىكا ئىسپانچە\u0001fr-CA\u0001كانادا فىرانسۇزچە\u0001fr-CH\u0001شىۋىتسارىيە فىرانسۇزچە\u0001pt-BR\u0001بىرازىلىيە پورتۇگالچە\u0001pt-PT\u0001ياۋروپا پورتۇگالچە\u0001zh-Hans\u0001ئاددىي خەنچە\u0001zh-Hant\u0001مۇرەككەپ خەنچە",
        "uk" to
            "de-CH\u0001верхньонімецька (Швейцарія)\u0001nl-BE\u0001фламандська\u0001zh-Hans\u0001китайська (спрощене письмо)\u0001zh-Hant\u0001китайська (традиційне письмо)",
        "ur" to
            "de-AT\u0001آسٹریائی جرمن\u0001de-CH\u0001سوئس ہائی جرمن\u0001en-AU\u0001آسٹریلیائی انگریزی\u0001en-CA\u0001کینیڈین انگریزی\u0001en-GB\u0001برطانوی انگریزی\u0001en-US\u0001امریکی انگریزی\u0001es-ES\u0001یورپی ہسپانوی\u0001es-MX\u0001میکسیکن ہسپانوی\u0001fr-CA\u0001کینیڈین فرانسیسی\u0001fr-CH\u0001سوئس فرینچ\u0001nl-BE\u0001فلیمِش\u0001pt-BR\u0001برازیلی پرتگالی\u0001pt-PT\u0001یورپی پرتگالی\u0001zh-Hans\u0001چینی (آسان کردہ)\u0001zh-Hant\u0001روایتی چینی",
        "uz" to
            "de-AT\u0001nemis (Avstriya)\u0001de-CH\u0001yuqori nemis (Shveytsariya)\u0001en-AU\u0001ingliz (Avstraliya)\u0001en-CA\u0001ingliz (Kanada)\u0001en-GB\u0001ingliz (Britaniya)\u0001en-US\u0001ingliz (Amerika)\u0001es-ES\u0001ispan (Yevropa)\u0001es-MX\u0001ispan (Meksika)\u0001fr-CA\u0001fransuz (Kanada)\u0001fr-CH\u0001fransuz (Shveytsariya)\u0001nl-BE\u0001flamand\u0001pt-BR\u0001portugal (Braziliya)\u0001pt-PT\u0001portugal (Yevropa)\u0001zh-Hant\u0001xitoy (an’anaviy)",
        "vi" to
            "de-CH\u0001Tiếng Thượng Giéc-man (Thụy Sĩ)\u0001en-GB\u0001Tiếng Anh (Anh)\u0001en-US\u0001Tiếng Anh (Mỹ)\u0001es-ES\u0001Tiếng Tây Ban Nha (Châu Âu)\u0001pt-PT\u0001Tiếng Bồ Đào Nha (Châu Âu)",
        "yo" to
            "de-AT\u0001Èdè Jámánì (Ọ́síríà )\u0001de-CH\u0001Èdè Ilẹ̀ Jámánì (Orílẹ́ède swítsàlandì)\u0001en-AU\u0001Èdè Gẹ̀ẹ́sì (órílẹ̀-èdè Ọsirélíà)\u0001en-CA\u0001Èdè Gẹ̀ẹ́sì (Orílẹ̀-èdè Kánádà)\u0001en-GB\u0001Èdè òyìnbó Gẹ̀ẹ́sì\u0001es-ES\u0001Èdè Sípáníìṣì (orílẹ̀-èdè Yúróòpù)\u0001es-MX\u0001Èdè Sípáníìṣì (orílẹ̀-èdè Mẹ́síkò)\u0001fr-CA\u0001Èdè Faransé (orílẹ̀-èdè Kánádà)\u0001fr-CH\u0001Èdè Faranṣé (Súwísàlaǹdì)\u0001nl-BE\u0001Èdè Flemiṣi\u0001pt-BR\u0001Èdè Pọtogí (Orilẹ̀-èdè Bràsíl)\u0001pt-PT\u0001Èdè Pọtogí (orílẹ̀-èdè Yúróòpù)\u0001zh-Hans\u0001Ẹdè Ṣáínà Onírọ̀rùn\u0001zh-Hant\u0001Èdè Ṣáínà Ìbílẹ̀",
        "zh" to
            "de-AT\u0001奥地利德语\u0001de-CH\u0001瑞士高地德语\u0001en-AU\u0001澳大利亚英语\u0001en-CA\u0001加拿大英语\u0001en-GB\u0001英国英语\u0001en-US\u0001美国英语\u0001es-ES\u0001欧洲西班牙语\u0001es-MX\u0001墨西哥西班牙语\u0001fr-CA\u0001加拿大法语\u0001fr-CH\u0001瑞士法语\u0001nl-BE\u0001弗拉芒语\u0001pt-BR\u0001巴西葡萄牙语\u0001pt-PT\u0001欧洲葡萄牙语\u0001zh-Hans\u0001简体中文\u0001zh-Hant\u0001繁体中文",
        "zu" to
            "de-AT\u0001isi-Austrian German\u0001de-CH\u0001Isi-Swiss High German\u0001en-AU\u0001i-Australian English\u0001en-CA\u0001i-Canadian English\u0001en-GB\u0001i-British English\u0001en-US\u0001i-American English\u0001es-ES\u0001isi-European Spanish\u0001es-MX\u0001isi-Mexican Spanish\u0001fr-CA\u0001isi-Canadian French\u0001fr-CH\u0001isi-Swiss French\u0001nl-BE\u0001isi-Flemish\u0001pt-BR\u0001isi-Brazillian Portuguese\u0001pt-PT\u0001isi-European Portuguese\u0001zh-Hans\u0001isi-Chinese (esenziwe-lula)\u0001zh-Hant\u0001Isi-Chinese Sasendulo",
    )

    private val byLanguage = LazyMap<String, Map<String, String>>(
        packed.mapValues { (_, blob) -> { parse(blob) } }
    )

    /** The number of names this table holds, counted from the payload so it cannot go stale. */
    internal val size: Int by lazy {
        packed.values.sumOf { blob -> (blob.count { it == '\u0001' } + 1) / 2 }
    }

    /** The dialect name of [languageTag] in [displayLanguage], or `null` if CLDR has none. */
    fun nameFor(displayLanguage: String, languageTag: String): String? =
        byLanguage[displayLanguage]?.get(languageTag)

    private fun parse(blob: String): Map<String, String> {
        val parts = blob.split('\u0001')
        val out = LinkedHashMap<String, String>(parts.size / 2)
        var i = 0
        while (i + 1 < parts.size) {
            out[parts[i]] = parts[i + 1]
            i += 2
        }
        return out
    }
}
