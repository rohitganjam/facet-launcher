package com.facetlauncher.app.data.iconpack

import org.xmlpull.v1.XmlPullParser

/** One titled group of a pack's icons, as its `drawable.xml` lists them. An untitled group has a null [title]. */
data class PackCategory(val title: String?, val drawables: List<String>)

/**
 * Every icon an installed pack offers, for browsing: its `drawable.xml` categories, or, for a pack without one, every
 * drawable its `appfilter.xml` names, as one untitled group.
 */
data class PackCatalog(val categories: List<PackCategory>) {
    /** Each drawable once, in the order the pack lists them. */
    val allDrawables: List<String> = categories.flatMap { it.drawables }.distinct()

    /** Drawables whose name holds every word of [query], `google_maps` reading as "google maps". */
    fun search(query: String): List<String> {
        val words = query.lowercase().split(' ', '_', '-').filter { it.isNotBlank() }
        if (words.isEmpty()) return allDrawables
        return allDrawables.filter { name -> words.all { it in name.lowercase() } }
    }

    companion object {
        /** Reads a plain-text `drawable.xml` (a pack's asset) with the same flat-tag scan [AppFilter.parse] uses. */
        fun parse(text: String): PackCatalog {
            val builder = Builder()
            val withoutComments = COMMENT.replace(text, "")
            TAG.findAll(withoutComments).forEach { match ->
                val attributes = ATTRIBUTE.findAll(match.groupValues[2]).associate { it.groupValues[1] to decode(it.groupValues[3]) }
                builder.tag(match.groupValues[1], attributes::get)
            }
            return builder.build()
        }

        /** Reads a compiled `drawable.xml` resource. */
        fun parse(parser: XmlPullParser): PackCatalog {
            val builder = Builder()
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType != XmlPullParser.START_TAG) continue
                builder.tag(parser.name) { parser.getAttributeValue(null, it) }
            }
            return builder.build()
        }

        /** Every drawable [filter] names, for a pack that ships no `drawable.xml`. */
        fun fromAppFilter(filter: AppFilter): PackCatalog =
            PackCatalog(listOf(PackCategory(null, (filter.components.values + filter.calendars.values.map { "${it}1" }).distinct())))

        private val COMMENT = Regex("(?s)<!--.*?-->")
        private val TAG = Regex("<(category|item)\\b([^>]*)>")
        private val ATTRIBUTE = Regex("([\\w:]+)\\s*=\\s*([\"'])(.*?)\\2")

        private fun decode(value: String): String =
            if ('&' !in value) value else value.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'").replace("&amp;", "&")
    }

    private class Builder {
        private val categories = mutableListOf<PackCategory>()
        private var title: String? = null
        private var drawables = mutableListOf<String>()

        fun tag(name: String, attribute: (String) -> String?) {
            when (name) {
                "category" -> {
                    flush()
                    title = attribute("title")?.takeIf { it.isNotBlank() }
                }
                "item" -> attribute("drawable")?.trim()?.takeIf { it.isNotEmpty() }?.let(drawables::add)
            }
        }

        fun build(): PackCatalog {
            flush()
            return PackCatalog(categories)
        }

        private fun flush() {
            if (drawables.isNotEmpty()) categories += PackCategory(title, drawables.distinct())
            drawables = mutableListOf()
        }
    }
}
