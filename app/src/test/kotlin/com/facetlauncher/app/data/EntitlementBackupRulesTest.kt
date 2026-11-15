package com.facetlauncher.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class EntitlementBackupRulesTest {

    private val entitlementFile = "datastore/facet_entitlement.preferences_pb"
    private val vaultFile = "datastore/facet_vault.preferences_pb"

    private fun excludedPaths(section: String): List<String> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File("src/main/res/xml/data_extraction_rules.xml"))
        return excludes(doc.getElementsByTagName(section).item(0) as Element)
    }

    private fun excludes(parent: Element): List<String> {
        val nodes = parent.getElementsByTagName("exclude")
        return (0 until nodes.length).map { (nodes.item(it) as Element).getAttribute("path") }
    }

    @Test
    fun `cloud backup leaves out the cached Pro flag and the vault`() {
        // Given the app's backup rules

        // When we read what cloud backup excludes
        val excluded = excludedPaths("cloud-backup")

        // Then the entitlement DataStore is excluded
        assertEquals(listOf(entitlementFile, vaultFile), excluded)
    }

    @Test
    fun `device transfer leaves out the cached Pro flag and the vault`() {
        assertEquals(listOf(entitlementFile, vaultFile), excludedPaths("device-transfer"))
    }

    @Test
    fun `Android 11 auto backup leaves out the same two files`() {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File("src/main/res/xml/backup_rules.xml"))

        assertEquals(listOf(entitlementFile, vaultFile), excludes(doc.documentElement))
    }

    @Test
    fun `the manifest points at the backup rules`() {
        val manifest = File("src/main/AndroidManifest.xml").readText()

        assertTrue(manifest.contains("android:dataExtractionRules=\"@xml/data_extraction_rules\""))
        assertTrue(manifest.contains("android:fullBackupContent=\"@xml/backup_rules\""))
    }
}
