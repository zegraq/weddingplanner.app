package app.weddingplanner.ui.budget

import java.time.LocalDate

internal fun formatSek(amount: Long?): String {
    if (amount == null) return "—"
    val grouped = kotlin.math.abs(amount).toString()
        .reversed()
        .chunked(3)
        .joinToString(" ")
        .reversed()
    val sign = if (amount < 0) "-" else ""
    return "$sign$grouped kr"
}

internal fun parseSek(text: String): Long? {
    val cleaned = text.filter { it.isDigit() || it == '-' }
    return cleaned.toLongOrNull()
}

internal fun todayIso(): String = LocalDate.now().toString()
