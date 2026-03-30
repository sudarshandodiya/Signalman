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
        if (!rule.isTransformEnabled) {
            return uri
        }

        if (rule.transformMode == TransformMode.SIMPLE) {
            val enabledReplacements = rule.urlComponentReplacements.filter { it.isEnabled }
            if (enabledReplacements.isNotEmpty()) {
                val components = UrlComponents.fromUri(uri)
                var currentComponents = components

                enabledReplacements.forEach { replacement ->
                    currentComponents =
                        when (replacement.component) {
                            UrlComponent.SCHEME -> currentComponents.copy(scheme = replacement.replacement)
                            UrlComponent.HOST -> currentComponents.copy(host = replacement.replacement)
                            UrlComponent.DOMAIN -> currentComponents.copy(domain = replacement.replacement)
                            UrlComponent.PORT -> currentComponents.copy(port = replacement.replacement)
                            UrlComponent.PATH -> currentComponents.copy(path = replacement.replacement)
                            UrlComponent.QUERY -> currentComponents.copy(query = replacement.replacement)
                            UrlComponent.FRAGMENT -> currentComponents.copy(fragment = replacement.replacement)
                            UrlComponent.USER_INFO -> currentComponents.copy(userInfo = replacement.replacement)
                        }
                }
                return currentComponents.toUri()
            }
            return uri
        }

        if (rule.transformMode == TransformMode.ADVANCED) {
            if (rule.replacePattern.isNullOrEmpty() || rule.replacement == null) {
                return uri
            }

            return try {
                val urlString = uri.toString()
                val newUrl =
                    if (rule.matchType == MatchType.REGEX) {
                        urlString.replace(Regex(rule.replacePattern), rule.replacement)
                    } else {
                        urlString.replace(rule.replacePattern, rule.replacement)
                    }
                Uri.parse(newUrl)
            } catch (e: Exception) {
                android.util.Log.e("TransformUrlUseCase", "Error applying rule transformation", e)
                uri
            }
        }

        return uri
    }
}
