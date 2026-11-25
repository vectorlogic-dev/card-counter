package com.vectorlogic.blackjackcounter.presentation

import com.vectorlogic.blackjackcounter.R
import java.io.File
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

/**
 * Resolves [UiText] to the English text in res/values/strings.xml, so JVM unit tests can check
 * real wording without an Android runtime. Follows Android's rules for the escapes we use
 * (\' \" \n) and English plurals (one / other).
 */
object TestStrings {
    private val stringNamesById: Map<Int, String> = namesById(R.string::class.java)
    private val pluralNamesById: Map<Int, String> = namesById(R.plurals::class.java)

    private val document by lazy {
        // Gradle runs unit tests from the module directory.
        val file = File("src/main/res/values/strings.xml")
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).documentElement
    }

    private val strings: Map<String, String> by lazy {
        val nodes = document.getElementsByTagName("string")
        (0 until nodes.length).associate { i ->
            val element = nodes.item(i) as Element
            element.getAttribute("name") to unescape(element.textContent)
        }
    }

    private val plurals: Map<String, Map<String, String>> by lazy {
        val nodes = document.getElementsByTagName("plurals")
        (0 until nodes.length).associate { i ->
            val element = nodes.item(i) as Element
            val items = element.getElementsByTagName("item")
            element.getAttribute("name") to (0 until items.length).associate { j ->
                val item = items.item(j) as Element
                item.getAttribute("quantity") to unescape(item.textContent)
            }
        }
    }

    fun resolve(text: UiText): String = when (text) {
        is UiText.Res -> format(string(text.id), text.args)
        is UiText.Plural -> {
            val name = pluralNamesById.getValue(text.id)
            val forms = plurals[name] ?: error("No <plurals name=\"$name\"> in strings.xml")
            val form = if (text.count == 1) forms["one"] else forms["other"]
            format(form ?: error("Plural $name has no form for ${text.count}"), text.args)
        }
    }

    fun string(id: Int): String {
        val name = stringNamesById.getValue(id)
        return strings[name] ?: error("No <string name=\"$name\"> in strings.xml")
    }

    private fun format(template: String, args: List<Any>): String {
        if (args.isEmpty()) return template
        val resolved = args.map { if (it is UiText) resolve(it) else it }.toTypedArray()
        return String.format(Locale.US, template, *resolved)
    }

    private fun unescape(raw: String): String = raw
        .trim()
        .replace("\\'", "'")
        .replace("\\\"", "\"")
        .replace("\\n", "\n")
        .replace("\\@", "@")
        .replace("\\?", "?")

    private fun namesById(rClass: Class<*>): Map<Int, String> =
        rClass.fields.associate { field -> field.getInt(null) to field.name }
}

/** English text for this [UiText], as the app shows it. */
val UiText.text: String get() = TestStrings.resolve(this)
