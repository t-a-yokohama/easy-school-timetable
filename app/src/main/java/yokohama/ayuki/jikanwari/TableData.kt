package yokohama.ayuki.jikanwari

import kotlinx.serialization.Serializable

@Serializable
data class Grades(
    val first: Terms,
    val second: Terms,
    val third: Terms,
    val fourth: Terms,
    val fifth: Terms,
    val sixth: Terms,
)

@Serializable
data class Terms(
    val prophase: Day,
    val late: Day,
    val spring: Day,
    val summer: Day,
    val autumn: Day,
    val winter: Day,
    val first: Day,
    val second: Day,
    val third: Day,
    val fourth: Day,
)

@Serializable
data class Day(
    val mon: Period,
    val tue: Period,
    val wed: Period,
    val thu: Period,
    val fri: Period,
    val sat: Period,
    val sun: Period,
)

@Serializable
data class Period(
    val subject: MutableList<String>,
    val room: MutableList<String>,
    val teacher: MutableList<String>,
    val color: MutableList<String>,
    val email: MutableList<String>,
)

@Serializable
data class SpinnerPosition(
    var grade: Int,
    var term: Int,
)
