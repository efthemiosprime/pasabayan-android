package com.efthemiosprime.pasabayan.shared.model

import androidx.annotation.StringRes
import com.efthemiosprime.pasabayan.R

data class CountryOption(
    val code: String,
    @StringRes val labelRes: Int,
)

object CountryCatalog {
    const val CODE_CANADA = "CA"
    const val CODE_PHILIPPINES = "PH"
    const val CODE_INDIA = "IN"

    val supportedCountries: List<CountryOption> = listOf(
        CountryOption(
            code = CODE_CANADA,
            labelRes = R.string.common_country_canada,
        ),
        CountryOption(
            code = CODE_PHILIPPINES,
            labelRes = R.string.common_country_philippines,
        ),
        CountryOption(
            code = CODE_INDIA,
            labelRes = R.string.common_country_india,
        ),
    )

    fun byCode(code: String): CountryOption =
        supportedCountries.firstOrNull { it.code == code } ?: supportedCountries.first()
}
