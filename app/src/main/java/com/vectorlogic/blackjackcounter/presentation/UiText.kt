package com.vectorlogic.blackjackcounter.presentation

import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalResources

/**
 * Text built outside Compose (view model, strategy engine, formatting helpers) without touching
 * Android resources, so that code stays unit-testable. Composables resolve it with [asString].
 * Arguments may themselves be [UiText]; they are resolved first.
 */
sealed interface UiText {
    data class Res(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText

    data class Plural(@PluralsRes val id: Int, val count: Int, val args: List<Any> = emptyList()) : UiText
}

fun uiText(@StringRes id: Int, vararg args: Any): UiText = UiText.Res(id, args.toList())

fun pluralText(@PluralsRes id: Int, count: Int, vararg args: Any): UiText =
    UiText.Plural(id, count, args.toList())

fun UiText.resolve(resources: Resources): String = when (this) {
    is UiText.Res -> if (args.isEmpty()) {
        resources.getString(id)
    } else {
        resources.getString(id, *args.map { it.resolveArg(resources) }.toTypedArray())
    }
    is UiText.Plural -> resources.getQuantityString(
        id,
        count,
        *args.map { it.resolveArg(resources) }.toTypedArray()
    )
}

private fun Any.resolveArg(resources: Resources): Any = if (this is UiText) resolve(resources) else this

@Composable
@ReadOnlyComposable
fun UiText.asString(): String = resolve(LocalResources.current)
