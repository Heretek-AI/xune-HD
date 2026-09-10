package com.heretek.xunehd

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Design-invariant audit, ported from Not-Zune's approach. Scans the UI
 * sources for violations of the Zune HD canon (docs/zune-hd-ui-canon.md §7).
 */
@RunWith(RobolectricTestRunner::class)
class DesignInvariantTest {

    private val appDir: File
        get() = if (File("src/main/java").exists()) File(".") else File("..")

    private val repoRoot: File
        get() = appDir.absoluteFile.normalize().parentFile

    private fun sources(dir: String): List<File> {
        val root = File(appDir, "src/main/java/com/heretek/xunehd/$dir")
        return root.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }

    @Test
    fun `no corner radius anywhere`() {
        val offenders = sources("")
            .filter { it.readText().contains("RoundedCornerShape") }
            .map { it.path }
        assertTrue(
            "RoundedCornerShape is banned (canon §7, invariant 1): $offenders",
            offenders.isEmpty(),
        )
    }

    @Test
    fun `screens consume tokens not raw colors`() {
        val offenders = (sources("ui") + sources("design/components") + sources("ui/apps"))
            .flatMap { file ->
                val text = file.readText()
                Regex("Color\\(0x[0-9A-Fa-f]{8}\\)").findAll(text)
                    .map { "${file.name}: ${it.value}" }
            }
            .toList()
        assertTrue(
            "Screens must use LocalXuneColors tokens, not raw colors: $offenders",
            offenders.isEmpty(),
        )
    }

    @Test
    fun `navigation uses motion tokens not springs`() {
        val offenders = (sources("ui") + sources("ui/apps"))
            .filter { it.readText().contains("spring") }
            .map { it.name }
        assertTrue(
            "Navigation must use XuneMotion deceleration (canon §6, invariant 6): $offenders",
            offenders.isEmpty(),
        )
    }

    @Test
    fun `mini-app registry is non-empty`() {
        val offenders = sources("ui/apps")
            .filter { it.name == "XuneApps.kt" }
            .filter { !it.readText().contains("XuneMiniApp(\"calculator\"") }
            .map { it.path }
        assertTrue("XuneApps registry should ship utilities (canon §8)", offenders.isEmpty())
    }

    @Test
    fun `canon documents exist`() {
        assertTrue(File(repoRoot, "docs/zune-hd-ui-canon.md").exists())
        assertTrue(File(repoRoot, "docs/design-tokens.md").exists())
        assertTrue(File(repoRoot, "docs/zcp-inventory.md").exists())
        assertTrue(File(repoRoot, "NOTICE.md").exists())
    }
}
