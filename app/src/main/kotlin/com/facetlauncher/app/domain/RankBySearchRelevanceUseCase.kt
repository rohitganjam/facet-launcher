package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.SearchSignals
import java.text.Normalizer
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * The one search model every search surface shares (drawer apps, contacts and settings, Private Space, the pickers). A match falls in a
 * [Tier]; results sort by tier, then (drawer apps only) by learned picks and launches, then alphabetically. See `12` §search.
 */
class RankBySearchRelevanceUseCase @Inject constructor() {
    // Labels barely change between keystrokes, so each is prepared once rather than on every query.
    private val prepared = ConcurrentHashMap<String, SearchableText>()

    private fun prepare(text: String): SearchableText {
        if (prepared.size > MAX_PREPARED) prepared.clear()
        return prepared.getOrPut(text) { SearchableText.of(text) }
    }

    /** Matches and ranks [items] by [key] alone. A blank [query] returns [items] unchanged. */
    operator fun <T> invoke(items: List<T>, query: String, key: (T) -> String): List<T> = rank(items, query, keys = { listOf(key(it)) })

    /** Drawer app search: a learned pick for this exact query goes first, and launches order apps within a tier. */
    fun apps(apps: List<AppInfo>, query: String, signals: SearchSignals): List<AppInfo> {
        val picks = signals.picks[normalizeForSearch(query)].orEmpty()
        return rank(apps, query, keys = { listOf(it.label) }) { app ->
            Boost(picks = picks[app.listKey] ?: 0, launches = signals.launchCounts[app.listKey] ?: 0)
        }
    }

    /** Matches [items] on any of [keys] (the best tier counts), so a setting can match on a keyword alias. */
    fun <T> rank(items: List<T>, query: String, keys: (T) -> List<String>, boost: (T) -> Boost = { Boost.NONE }): List<T> {
        if (query.isBlank()) return items
        val normalizedQuery = normalizeForSearch(query)
        if (normalizedQuery.isEmpty()) return emptyList()
        val queryTokens = normalizedQuery.split(' ')
        return items
            .mapNotNull { item ->
                val texts = keys(item)
                val tier = texts.mapNotNull { tierOf(prepare(it), normalizedQuery, queryTokens) }.minOrNull() ?: return@mapNotNull null
                Ranked(item, tier, boost(item), texts.firstOrNull()?.lowercase(Locale.ROOT).orEmpty())
            }
            .sortedWith(RANKED_ORDER)
            .map { it.item }
    }

    /** The query as search compares it, also the key a learned pick is stored under. */
    fun normalize(query: String): String = normalizeForSearch(query)

    /** What use has taught search about one item; [picks] is for the current query. */
    data class Boost(val picks: Int = 0, val launches: Int = 0) {
        companion object {
            val NONE = Boost()
        }
    }

    /** How a query matched, best first. */
    enum class Tier { EXACT, PREFIX, WORD_PREFIX, INITIALS, SUBSTRING }

    private class Ranked<T>(val item: T, val tier: Tier, val boost: Boost, val sortKey: String) {
        val learned: Boolean get() = boost.picks >= LEARNED_PICK_THRESHOLD
    }

    private companion object {
        /** Opening an app from the same query this many times moves it to the top for that query. */
        const val LEARNED_PICK_THRESHOLD = 2
        const val MAX_PREPARED = 4096

        val RANKED_ORDER = compareByDescending<Ranked<*>> { it.learned }
            .thenByDescending { if (it.learned) it.boost.picks else 0 }
            .thenBy { it.tier }
            .thenByDescending { it.boost.launches }
            .thenBy { it.sortKey }
    }
}

/** Whether [text] matches [query] under the shared model, for surfaces that filter but keep their own order (the pickers). */
fun matchesSearch(text: String, query: String): Boolean {
    if (query.isBlank()) return true
    val normalizedQuery = normalizeForSearch(query)
    return normalizedQuery.isNotEmpty() && tierOf(SearchableText.of(text), normalizedQuery, normalizedQuery.split(' ')) != null
}

/** Accents stripped, lowercased, every run of punctuation or spaces turned into one space, trimmed. */
internal fun normalizeForSearch(text: String): String =
    SEPARATORS.replace(stripAccents(text).lowercase(Locale.ROOT), " ").trim()

private val COMBINING_ACCENTS = Regex("[\\u0300-\\u036f]")
private val SEPARATORS = Regex("[^\\p{L}\\p{N}\\p{M}]+")
private val CAMEL_HUMP = Regex("(?<=\\p{Ll})(?=\\p{Lu})")

/** Only Latin-style combining accents go, so marks that are part of other scripts (Devanagari vowel signs) stay. */
private fun stripAccents(text: String): String = COMBINING_ACCENTS.replace(Normalizer.normalize(text, Normalizer.Form.NFD), "")

/** A label prepared for matching: its normalized form, its words (a capital inside a word starts a new one: WhatsApp is whats, app). */
internal class SearchableText(val normalized: String, val words: List<String>) {
    val initials: String = words.joinToString("") { it.take(1) }

    /** Without spaces, so "wifi" finds Wi-Fi. */
    val compact: String = normalized.replace(" ", "")

    companion object {
        fun of(text: String) = SearchableText(
            normalized = normalizeForSearch(text),
            words = normalizeForSearch(CAMEL_HUMP.replace(stripAccents(text), " ")).split(' ').filter { it.isNotEmpty() },
        )
    }
}

private fun tierOf(text: SearchableText, query: String, queryTokens: List<String>): RankBySearchRelevanceUseCase.Tier? {
    val compactQuery = query.replace(" ", "")
    return when {
        text.normalized.isEmpty() -> null
        text.normalized == query || text.compact == compactQuery -> RankBySearchRelevanceUseCase.Tier.EXACT
        text.normalized.startsWith(query) || text.compact.startsWith(compactQuery) -> RankBySearchRelevanceUseCase.Tier.PREFIX
        text.words.any { it.startsWith(query) } || tokensStartWordsInOrder(text.words, queryTokens) -> RankBySearchRelevanceUseCase.Tier.WORD_PREFIX
        // One letter only matches the start of the label or a word; "m" shouldn't list every app with an m in it.
        query.length < 2 -> null
        else -> innerTierOf(text, query, compactQuery)
    }
}

private fun innerTierOf(text: SearchableText, query: String, compactQuery: String): RankBySearchRelevanceUseCase.Tier? = when {
    ' ' !in query && text.words.size > 1 && text.initials.contains(query) -> RankBySearchRelevanceUseCase.Tier.INITIALS
    text.normalized.contains(query) || text.compact.contains(compactQuery) -> RankBySearchRelevanceUseCase.Tier.SUBSTRING
    else -> null
}

/** "goo ma" matches Google Maps: each query word starts a later word of the label than the one before. */
private fun tokensStartWordsInOrder(words: List<String>, tokens: List<String>): Boolean {
    if (tokens.size < 2) return false
    var next = 0
    for (token in tokens) {
        val found = (next until words.size).firstOrNull { words[it].startsWith(token) } ?: return false
        next = found + 1
    }
    return true
}
