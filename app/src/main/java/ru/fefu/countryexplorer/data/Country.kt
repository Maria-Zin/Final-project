package ru.fefu.countryexplorer.data

data class Country(
    val name: String,
    val capital: String?,
    val population: Long,
    val area: Double?,
    val region: String,
    val flags: Flags? = null
) {
    val stableId: String get() = name.replace(" ", "_")
}

data class Flags(
    val svg: String,
    val png: String
)