package com.facetlauncher.app.data.dailyphotos

import com.facetlauncher.app.data.model.DailyPhotoSource
import java.io.File
import java.security.MessageDigest
import java.time.Clock
import java.time.LocalDate

/** A feed as stored: the JSON, and the calendar day it was fetched on. */
data class CachedFeed(val json: String, val day: LocalDate)

/**
 * A day's feed and thumbnails on disk under [root] (the app's cache folder, which the system may clear: that only costs one more fetch). A feed
 * is current for the rest of the calendar day it was fetched on; the first fetch of a new day replaces it and drops the thumbnails it no longer lists.
 */
class DailyPhotoCache(private val root: File, private val clock: Clock) {

    fun today(): LocalDate = LocalDate.now(clock)

    private fun dir(source: DailyPhotoSource) = File(root, source.name.lowercase())
    private fun feedFile(source: DailyPhotoSource, country: String) = File(dir(source), "feed_$country.txt")
    private fun thumbFile(source: DailyPhotoSource, url: String) = File(File(dir(source), "thumbs"), sha1(url))

    fun readFeed(source: DailyPhotoSource, country: String): CachedFeed? = runCatching {
        val file = feedFile(source, country)
        if (!file.isFile) return@runCatching null
        val text = file.readText()
        val split = text.indexOf('\n')
        if (split < 0) return@runCatching null
        CachedFeed(json = text.substring(split + 1), day = LocalDate.parse(text.substring(0, split)))
    }.getOrNull()

    /** Stores [json] as fetched today, and drops every thumbnail of [source] whose address is not in [keepThumbUrls]. */
    fun writeFeed(source: DailyPhotoSource, country: String, json: String, keepThumbUrls: List<String>) {
        runCatching {
            val file = feedFile(source, country)
            file.parentFile?.mkdirs()
            file.writeText("${today()}\n$json")
            val keep = keepThumbUrls.map(::sha1).toSet()
            File(dir(source), "thumbs").listFiles()?.filter { it.name !in keep }?.forEach { it.delete() }
        }
    }

    fun readThumbnail(source: DailyPhotoSource, url: String): ByteArray? = runCatching { thumbFile(source, url).takeIf { it.isFile }?.readBytes() }.getOrNull()

    fun writeThumbnail(source: DailyPhotoSource, url: String, bytes: ByteArray) {
        runCatching {
            val file = thumbFile(source, url)
            file.parentFile?.mkdirs()
            file.writeBytes(bytes)
        }
    }

    private fun sha1(text: String): String = MessageDigest.getInstance("SHA-1").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }
}
