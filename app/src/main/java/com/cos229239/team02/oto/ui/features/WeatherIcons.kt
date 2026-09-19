package com.cos229239.team02.oto.ui.features




fun weatherIcon(
    code: Int?,
    isDay: Boolean?,
): String = when (code) {
    0 -> when (isDay) {
        true -> "☀️"
        false -> "🌙"
        null -> "🌤️"
}
    1,2 -> if (isDay == false) "☁️" else "☁️"
    3 -> "☁️"
    45, 48 -> "🌫️"
    51, 53, 55 -> "🌦️"
    56, 57, 66, 67 -> "🧊"
    61, 63, 65, 80, 81, 82 -> "🌧️"
    71, 73, 75, 77, 85, 86 -> "🌨️"
    95, 96, 99 -> "⛈️"
    else -> "🌡️"

}