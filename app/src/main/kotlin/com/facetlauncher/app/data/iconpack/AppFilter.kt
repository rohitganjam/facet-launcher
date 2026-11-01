package com.facetlauncher.app.data.iconpack

import org.xmlpull.v1.XmlPullParser

/**
 * An icon pack's `appfilter.xml`: which drawable stands in for which activity, plus the optional
 * art (`iconback`/`iconmask`/`iconupon`/`scale`) packs use to theme apps they have no icon for.
 */
data class AppFilter(
    /** Drawable name by `"package/activityClass"`. */
    val components: Map<String, String>,
    val iconBacks: List<String> = emptyList(),
    val iconMask: String? = null,
    val iconUpon: String? = null,
    val scale: Float = 1f,
    /** `<calendar>` entries: the drawable prefix by `"package/activityClass"`, completed with the day of the month (`calendar_` + `14`). */
    val calendars: Map<String, String> = emptyMap(),
) {
    /** The first drawable listed for each package — the stand-in when an app's exact activity isn't listed. */
    val packages: Map<String, String> = components.byPackage()

    private val calendarPackages: Map<String, String> = calendars.byPackage()

    val hasFallbackArt: Boolean get() = iconBacks.isNotEmpty() || iconMask != null || iconUpon != null

    /** The drawable for this activity, else for its package, else null. */
    fun drawableFor(packageName: String, className: String): String? =
        components["$packageName/$className"] ?: packages[packageName]

    /** The day-of-month drawable prefix for this activity (else its package), null for an app the pack doesn't date. */
    fun calendarPrefixFor(packageName: String, className: String): String? =
        calendars["$packageName/$className"] ?: calendarPackages[packageName]

    private fun Map<String, String>.byPackage(): Map<String, String> = buildMap {
        this@byPackage.forEach { (key, value) -> putIfAbsent(key.substringBefore('/'), value) }
    }

    /** Collects tags from either reader; malformed entries are skipped rather than failing the whole pack. */
    private class Builder(private val keepPackage: (String) -> Boolean) {
        private val components = LinkedHashMap<String, String>()
        private val calendars = LinkedHashMap<String, String>()
        private val iconBacks = mutableListOf<String>()
        private var iconMask: String? = null
        private var iconUpon: String? = null
        private var scale = 1f

        /** [attribute] looks one up by name; [images] lists the `img1`, `img2`... values in order. */
        fun tag(name: String, attribute: (String) -> String?, images: () -> List<String>) {
            when (name) {
                "item" -> {
                    val key = componentKey(attribute("component"))?.takeIf { keepPackage(it.substringBefore('/')) } ?: return
                    val drawable = attribute("drawable")?.takeIf { it.isNotBlank() } ?: return
                    components.putIfAbsent(key, drawable)
                }
                "calendar" -> {
                    val key = componentKey(attribute("component"))?.takeIf { keepPackage(it.substringBefore('/')) } ?: return
                    val prefix = attribute("prefix")?.takeIf { it.isNotBlank() } ?: return
                    calendars.putIfAbsent(key, prefix)
                }
                "iconback" -> iconBacks += images()
                "iconmask" -> iconMask = images().firstOrNull() ?: iconMask
                "iconupon" -> iconUpon = images().firstOrNull() ?: iconUpon
                "scale" -> scale = attribute("factor")?.toFloatOrNull()?.takeIf { it > 0f } ?: scale
            }
        }

        fun build() = AppFilter(components, iconBacks, iconMask, iconUpon, scale, calendars)
    }

    companion object {
        /** Tags whose attributes are read; every other tag is passed over. */
        private val READ_TAGS = setOf("item", "calendar", "iconback", "iconmask", "iconupon", "scale")

        /**
         * Reads a compiled `appfilter.xml` resource. Only items whose package passes [keepPackage] are held: a big pack
         * lists tens of thousands of apps, almost none of them installed.
         */
        fun parse(parser: XmlPullParser, keepPackage: (String) -> Boolean = { true }): AppFilter {
            val builder = Builder(keepPackage)
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType != XmlPullParser.START_TAG || parser.name !in READ_TAGS) continue
                builder.tag(
                    parser.name,
                    attribute = { parser.getAttributeValue(null, it) },
                    images = {
                        (0 until parser.attributeCount)
                            .filter { parser.getAttributeName(it).startsWith("img") }
                            .mapNotNull { parser.getAttributeValue(it)?.takeIf(String::isNotBlank) }
                    },
                )
            }
            return builder.build()
        }

        /**
         * Reads a plain-text `appfilter.xml` (a pack's asset) by scanning for its tags. Android's pull parser takes
         * seconds over a pack listing ~50k apps; packs only use flat, attribute-only tags, so a scan is enough.
         */
        fun parse(text: String, keepPackage: (String) -> Boolean = { true }): AppFilter {
            val builder = Builder(keepPackage)
            var open = text.indexOf('<')
            while (open >= 0) {
                open = if (text.startsWith("<!--", open)) {
                    text.indexOf("-->", open + 4).let { end -> if (end < 0) -1 else text.indexOf('<', end + 3) }
                } else {
                    val close = text.indexOf('>', open)
                    if (close >= 0) readTag(text, open, close, builder)
                    if (close < 0) -1 else text.indexOf('<', close + 1)
                }
            }
            return builder.build()
        }

        /** The tag between `<` at [open] and `>` at [close], if it is one [READ_TAGS] names. */
        private fun readTag(text: String, open: Int, close: Int, builder: Builder) {
            var nameEnd = open + 1
            while (nameEnd < close && !text[nameEnd].isWhitespace() && text[nameEnd] != '/') nameEnd++
            val name = text.substring(open + 1, nameEnd)
            if (name !in READ_TAGS) return
            val attributes = attributes(text, nameEnd, close)
            builder.tag(
                name,
                attribute = attributes::get,
                images = { attributes.filterKeys { it.startsWith("img") }.values.filter(String::isNotBlank) },
            )
        }

        /** `name="value"` pairs between [start] and [end], in order, with XML's predefined entities decoded. */
        private fun attributes(text: String, start: Int, end: Int): Map<String, String> {
            val result = LinkedHashMap<String, String>()
            var index = start
            while (index < end) {
                val equals = text.indexOf('=', index).takeIf { it in index until end }
                val quoteAt = equals?.let { (it + 1 until end).firstOrNull { i -> text[i] == '"' || text[i] == '\'' } }
                val valueEnd = quoteAt?.let { text.indexOf(text[it], it + 1).takeIf { e -> e in it until end } }
                if (equals == null || quoteAt == null || valueEnd == null) break
                result[text.substring(index, equals).trim()] = decodeEntities(text.substring(quoteAt + 1, valueEnd))
                index = valueEnd + 1
            }
            return result
        }

        private fun decodeEntities(value: String): String =
            if ('&' !in value) value else value.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'").replace("&amp;", "&")

        /** `ComponentInfo{com.app/com.app.Main}` (or `.Main`, relative to the package; stray doubled braces happen in real packs) → `com.app/com.app.Main`. */
        internal fun componentKey(raw: String?): String? {
            val inner = raw?.trim()?.removePrefix("ComponentInfo")?.trim('{', '}') ?: return null
            val packageName = inner.substringBefore('/', "").trim()
            val className = inner.substringAfter('/', "").trim()
            if (packageName.isEmpty() || className.isEmpty()) return null
            return "$packageName/${if (className.startsWith('.')) packageName + className else className}"
        }
    }
}
