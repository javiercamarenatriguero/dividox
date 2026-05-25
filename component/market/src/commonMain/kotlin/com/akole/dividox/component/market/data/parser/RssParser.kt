package com.akole.dividox.component.market.data.parser

import com.akole.dividox.component.market.domain.model.NewsItem
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

internal object RssParser {

    fun parseNewsItems(rssXml: String, maxCount: Int = 10): List<NewsItem> {
        val itemRegex = Regex("<item>(.*?)</item>", RegexOption.DOT_MATCHES_ALL)
        return itemRegex.findAll(rssXml)
            .take(maxCount)
            .mapNotNull { parseItem(it.groupValues[1]) }
            .toList()
    }

    private fun parseItem(content: String): NewsItem? {
        val id = extractTag(content, "guid") ?: return null
        val title = extractTag(content, "title")?.unescapeHtml() ?: return null
        val link = extractTag(content, "link") ?: return null
        val description = extractTag(content, "description")?.unescapeHtml()?.takeIf { it.isNotBlank() }
        val pubDate = extractTag(content, "pubDate")
        return NewsItem(
            id = id,
            title = title,
            publisher = publisherFromLink(link),
            link = link,
            publishedAt = pubDate?.let { parseRfc2822(it) } ?: Instant.DISTANT_PAST,
            thumbnailUrl = null,
            summary = description,
        )
    }

    private fun extractTag(content: String, tag: String): String? {
        val regex = Regex(
            "<$tag[^>]*><!\\[CDATA\\[(.*?)\\]\\]></$tag>|<$tag[^>]*>(.*?)</$tag>",
            RegexOption.DOT_MATCHES_ALL,
        )
        val match = regex.find(content) ?: return null
        return (match.groupValues[1].ifEmpty { match.groupValues[2] }).trim().ifEmpty { null }
    }

    private fun publisherFromLink(link: String): String {
        val host = link.removePrefix("https://").removePrefix("http://")
            .substringBefore("/")
            .removePrefix("www.")
        return if (host.contains("yahoo.com")) "Yahoo Finance" else host
    }

    private fun parseRfc2822(dateStr: String): Instant = try {
        val withoutDayOfWeek = if (dateStr.contains(", ")) dateStr.substringAfter(", ") else dateStr
        val parts = withoutDayOfWeek.trim().split(" ")
        val day = parts[0].toInt()
        val month = MONTHS[parts[1].lowercase()] ?: return Instant.DISTANT_PAST
        val year = parts[2].toInt()
        val (hour, minute, second) = parts[3].split(":").map { it.toInt() }
        LocalDateTime(year, month, day, hour, minute, second).toInstant(TimeZone.UTC)
    } catch (_: Exception) {
        Instant.DISTANT_PAST
    }

    private fun String.unescapeHtml(): String = replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace("&#39;", "'")

    private val MONTHS = mapOf(
        "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4,
        "may" to 5, "jun" to 6, "jul" to 7, "aug" to 8,
        "sep" to 9, "oct" to 10, "nov" to 11, "dec" to 12,
    )
}
