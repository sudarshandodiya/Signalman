package net.dodiya.signalman.domain

import android.net.Uri
import net.dodiya.signalman.data.MatchType
import net.dodiya.signalman.data.Rule
import net.dodiya.signalman.data.TransformMode
import net.dodiya.signalman.data.UrlComponent
import net.dodiya.signalman.data.UrlComponents

class TransformUrlUseCase {
    operator fun invoke(
        uri: Uri,
        rule: Rule,
    ): Uri {
        return when (val transformMode = rule.transformMode) {
            is TransformMode.Simple -> {
                val pattern = transformMode.replacePattern
                val replacement = transformMode.replacement

                if (pattern.isEmpty() || replacement.isEmpty()) {
                    return uri
                }

                val matchType = rule.filters.firstOrNull()?.matchType ?: MatchType.REGEX

                try {
                    val urlString = uri.toString()
                    if (matchType == MatchType.REGEX) {
                        Uri.parse(urlString.replace(Regex(pattern), replacement))
                    } else {
                        Uri.parse(urlString.replace(pattern, replacement))
                    }
                } catch (e: Exception) {
                    android.util.Log.e("TransformUrlUseCase", "Error applying rule transformation", e)
                    uri
                }
            }

            is TransformMode.Advanced -> {
                val enabledReplacements =
                    transformMode.urlComponentReplacements
                        .filter { it.isEnabled }
                        .associate { it.component to it.replacement }

                if (enabledReplacements.isEmpty()) {
                    return uri
                }

                val original = UrlComponents.fromUri(uri)
                UrlComponents(
                    scheme = enabledReplacements[UrlComponent.SCHEME] ?: original.scheme,
                    host = enabledReplacements[UrlComponent.HOST] ?: original.host,
                    domain = enabledReplacements[UrlComponent.DOMAIN] ?: original.domain,
                    port = enabledReplacements[UrlComponent.PORT] ?: original.port,
                    path = enabledReplacements[UrlComponent.PATH] ?: original.path,
                    query = enabledReplacements[UrlComponent.QUERY] ?: original.query,
                    fragment = enabledReplacements[UrlComponent.FRAGMENT] ?: original.fragment,
                    userInfo = enabledReplacements[UrlComponent.USER_INFO] ?: original.userInfo,
                ).toUri()
            }
        }
    }
}
