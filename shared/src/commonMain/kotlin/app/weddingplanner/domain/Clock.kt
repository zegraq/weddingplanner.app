package app.weddingplanner.domain

fun interface Clock {
    fun nowIso(): String
}
