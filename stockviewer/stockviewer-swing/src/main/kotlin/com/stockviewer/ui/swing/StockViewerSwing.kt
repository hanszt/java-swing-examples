package com.stockviewer.ui.swing

import com.stockviewer.client.DataFetchMode
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.util.*
import javax.swing.SwingUtilities
import javax.swing.UIManager

object StockViewerSwing {

    private val log = LoggerFactory.getLogger("StockViewer")

    @JvmStatic
    fun main(args: Array<String>) {
        log.info("Starting StockViewer with the following arguments: ${args.joinToString(", ")}")

        val profiles = args.getOrNull(0)?.split(',') ?: emptyList()
        val properties = loadProperties(profiles)

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        UIManager.put("ToggleButton.select", Theme.ACCENT)

        SwingUtilities.invokeLater {
            MainWindow(
                dataFetchMode = if ("mock" in profiles) DataFetchMode.MOCK_DATA else DataFetchMode.API_DATA,
                apiKey = properties.getProperty("api-key") ?: error("No API key provided")
            )
        }
    }

    private fun loadProperties(profiles: List<String>): Properties {
        val defaultProperties = "/application.properties"
        val mainProps = resourceAsStreamOrNull(defaultProperties) ?: error("No $defaultProperties found")
        val properties = Properties()
        properties.load(mainProps)
        profiles.forEach { profile ->
            resourceAsStreamOrNull("/application-$profile.properties")?.also {
                log.info("Loading profile: $it")
                properties.load(it)
            }
        }
        return properties
    }
}

private fun resourceAsStreamOrNull(location: String): InputStream? = {}::class.java.getResourceAsStream(location)
