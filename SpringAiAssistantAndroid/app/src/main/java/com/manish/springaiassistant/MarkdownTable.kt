
package com.manish.springaiassistant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownContent(text: String) {

    val lines = text.lines()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        var index = 0

        while (index < lines.size) {

            val currentLine = lines[index]
            val trimmedLine = currentLine.trim()

            if (trimmedLine.isEmpty()) {
                index++
                continue
            }

            // Markdown Table

            if (isMarkdownTableStart(lines, index)) {

                val tableLines = mutableListOf<String>()

                while (
                    index < lines.size &&
                    lines[index].contains("|") &&
                    lines[index].isNotBlank()
                ) {
                    tableLines.add(lines[index])
                    index++
                }

                val rows = tableLines
                    .filterNot { isSeparatorRow(it) }
                    .map { parseMarkdownRow(it) }

                if (rows.size >= 2) {
                    MarkdownTable(rows)
                }

                continue
            }

            // Headings

            val headingMatch = Regex(
                """^(#{1,3})\s+(.+)$"""
            ).find(trimmedLine)

            if (headingMatch != null) {

                val headingLevel = headingMatch.groupValues[1].length
                val headingText = headingMatch.groupValues[2]

                val headingStyle = when (headingLevel) {
                    1 -> MaterialTheme.typography.titleLarge
                    2 -> MaterialTheme.typography.titleMedium
                    else -> MaterialTheme.typography.titleSmall
                }

                MarkdownInlineText(
                    text = headingText,
                    style = headingStyle.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF233DAA)
                )

                index++
                continue
            }

            // Bullet Points

            val bulletMatch = Regex(
                """^[-*+•]\s+(.+)$"""
            ).find(trimmedLine)

            if (bulletMatch != null) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {

                    Text(
                        text = "•",
                        modifier = Modifier.padding(
                            end = 10.dp,
                            top = 1.dp
                        ),
                        color = Color(0xFF3659E3),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    MarkdownInlineText(
                        text = bulletMatch.groupValues[1],
                        modifier = Modifier.weight(1f)
                    )
                }

                index++
                continue
            }

            // Numbered Lists

            val numberedMatch = Regex(
                """^(\d+)[.)]\s+(.+)$"""
            ).find(trimmedLine)

            if (numberedMatch != null) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {

                    Text(
                        text = "${numberedMatch.groupValues[1]}.",
                        modifier = Modifier.padding(
                            end = 10.dp,
                            top = 2.dp
                        ),
                        color = Color(0xFF3659E3),
                        fontWeight = FontWeight.Bold
                    )

                    MarkdownInlineText(
                        text = numberedMatch.groupValues[2],
                        modifier = Modifier.weight(1f)
                    )
                }

                index++
                continue
            }

            // Normal Paragraph

            val paragraphLines = mutableListOf<String>()

            while (index < lines.size) {

                val line = lines[index]
                val trimmed = line.trim()

                if (trimmed.isEmpty()) break

                if (isMarkdownTableStart(lines, index)) break

                if (
                    Regex("""^#{1,3}\s+""").containsMatchIn(trimmed) ||
                    Regex("""^[-*+•]\s+""").containsMatchIn(trimmed) ||
                    Regex("""^\d+[.)]\s+""").containsMatchIn(trimmed)
                ) {
                    break
                }

                paragraphLines.add(trimmed)
                index++
            }

            if (paragraphLines.isNotEmpty()) {

                MarkdownInlineText(
                    text = paragraphLines.joinToString(" ")
                )
            }
        }
    }
}

// ---------------------------------------------
// Inline Markdown Formatting and Links
// Supports:
// Bold, Italic, Inline Code
// Markdown Links and Plain URLs
// ---------------------------------------------

@Composable
private fun MarkdownInlineText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = Color(0xFF30364A)
) {

    val annotatedText = buildAnnotatedString {

        val pattern = Regex(
            """\[([^\]]+)]\((https?://[^)\s]+)\)|(https?://[^\s<>]+)|(\*\*(.+?)\*\*|__(.+?)__|(?<!\*)\*([^*\n]+)\*(?!\*)|(?<!_)_([^_\n]+)_(?!_)|`([^`]+)`)"""
        )

        var currentIndex = 0

        pattern.findAll(text).forEach { match ->

            append(text.substring(currentIndex, match.range.first))

            val markdownLinkText = match.groups[1]?.value
            val markdownLinkUrl = match.groups[2]?.value
            val plainUrl = match.groups[3]?.value

            if (markdownLinkUrl != null && markdownLinkText != null) {

                // Markdown link:
                // [Read full article](https://example.com)

                withLink(
                    LinkAnnotation.Url(
                        url = markdownLinkUrl,
                        styles = androidx.compose.ui.text.TextLinkStyles(
                            style = SpanStyle(
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    )
                ) {
                    append(markdownLinkText)
                }

            } else if (plainUrl != null) {

                // Plain URL:
                // https://example.com
                // Display only "Read full article".

                val cleanUrl = plainUrl.trimEnd(
                    '.', ',', '!', '?', ';', ':'
                )

                val trailingText = plainUrl.removePrefix(cleanUrl)

                withLink(
                    LinkAnnotation.Url(
                        url = cleanUrl,
                        styles = androidx.compose.ui.text.TextLinkStyles(
                            style = SpanStyle(
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    )
                ) {
                    append("Read full article")
                }

                append(trailingText)

            } else {

                val boldText = match.groups[5]?.value
                    ?: match.groups[6]?.value

                val italicText = match.groups[7]?.value
                    ?: match.groups[8]?.value

                val codeText = match.groups[9]?.value

                when {

                    boldText != null -> {
                        pushStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        append(boldText)
                        pop()
                    }

                    italicText != null -> {
                        pushStyle(
                            SpanStyle(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        )
                        append(italicText)
                        pop()
                    }

                    codeText != null -> {
                        pushStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = Color(0xFFF0F2F7)
                            )
                        )
                        append(codeText)
                        pop()
                    }
                }
            }

            currentIndex = match.range.last + 1
        }

        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }

    Text(
        text = annotatedText,
        modifier = modifier,
        color = color,
        style = style,
        lineHeight = 23.sp
    )
}

// ---------------------------------------------
// Markdown Table
// ---------------------------------------------

@Composable
fun MarkdownTable(rows: List<List<String>>) {

    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()

    val columnCount = rows.maxOfOrNull { it.size } ?: 0

    if (columnCount == 0) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = Color(0xFFE0E4EF),
                shape = RoundedCornerShape(10.dp)
            )
    ) {

        Column(
            modifier = Modifier
                .horizontalScroll(horizontalScroll)
                .verticalScroll(verticalScroll)
        ) {

            rows.forEachIndexed { rowIndex, row ->

                Row(
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {

                    repeat(columnCount) { columnIndex ->

                        val cellText = row.getOrElse(columnIndex) {
                            ""
                        }

                        val isHeader = rowIndex == 0

                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .fillMaxHeight()
                                .defaultMinSize(minHeight = 48.dp)
                                .background(
                                    when {
                                        isHeader ->
                                            MaterialTheme.colorScheme.primary

                                        rowIndex % 2 == 0 ->
                                            Color(0xFFF5F7FC)

                                        else ->
                                            Color.White
                                    }
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = Color(0xFFE0E4EF)
                                )
                                .padding(
                                    horizontal = 10.dp,
                                    vertical = 10.dp
                                )
                        ) {

                            MarkdownInlineText(
                                text = cellText,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isHeader) {
                                    Color.White
                                } else {
                                    Color(0xFF30364A)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------
// Markdown Table Detection
// ---------------------------------------------

private fun isMarkdownTableStart(
    lines: List<String>,
    index: Int
): Boolean {

    if (index + 1 >= lines.size) return false

    return lines[index].contains("|") &&
            isSeparatorRow(lines[index + 1])
}

private fun isSeparatorRow(line: String): Boolean {

    val cells = parseMarkdownRow(line)

    return cells.isNotEmpty() && cells.all { cell ->
        cell.matches(Regex(":?-{3,}:?"))
    }
}

private fun parseMarkdownRow(line: String): List<String> {

    return line.trim()
        .removePrefix("|")
        .removeSuffix("|")
        .split("|")
        .map { it.trim() }
}
