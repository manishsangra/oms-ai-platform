
package com.manish.springaiassistant.utils

object WeatherRequestDetector {

    private val weatherPattern = Regex(
        """\b(?:weather(?:\s+(?:information|info|details|report|forecast))?|temperature|humidity)\s+(?:in|at|for|of)\s+(.+?)(?:\s+in\s+structured\s+format)?[?.!]*$""",
        RegexOption.IGNORE_CASE
    )

    fun extractCity(message: String): String? {

        val match = weatherPattern.find(message.trim())
            ?: return null

        return match.groupValues[1]
            .trim()
            .takeIf { it.isNotBlank() }
    }
}
