package io.github.aughtone.types.financial
import platform.linux.NL_CAT_LOCALE
actual fun currencyFor(currencyCode: String): Currency? {
    // XXX We can probably get the info we need from the shell. This needs to be worked out.
    // locale -a | while IFS= read -r loc; do if [[ $(locale -ck int_curr_symbol -L "$loc" 2>/dev/null) == *"USD "* ]]; then echo "Currency information for locale: $loc"; echo "-----------------------------------------"; locale -k -L "$loc" | grep -E "int_curr_symbol|currency_symbol|mon_decimal_point|mon_thousands_sep|positive_sign|negative_sign|frac_digits|p_cs_precedes|p_sep_by_space|n_cs_precedes|n_sep_by_space|p_sign_posn|n_sign_posn"; echo ""; fi; done
    TODO("Not yet implemented")
}
