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
    ): Uri =
        when (val transformMode = rule.transformMode) {
            is TransformMode.Simple -> applySimpleTransform(uri, rule, transformMode)
            is TransformMode.Advanced -> applyAdvancedTransform(uri, transformMode)
        }

    private fun applySimpleTransform(
        uri: Uri,
        rule: Rule,
        transformMode: TransformMode.Simple,
    ): Uri {
        val pattern = transformMode.replacePattern
        val replacement = transformMode.replacement

        if (pattern.isEmpty() || replacement.isEmpty()) {
            return uri
        }

        val matchType = rule.filters.firstOrNull()?.matchType ?: MatchType.REGEX
        return transformWithPattern(uri, pattern, replacement, matchType)
    }

    private fun transformWithPattern(
        uri: Uri,
        pattern: String,
        replacement: String,
        matchType: MatchType,
    ): Uri =
        try {
            val urlString = uri.toString()
            val transformedUrl =
                when (matchType) {
                    MatchType.REGEX -> urlString.replace(Regex(pattern), replacement)
                    else -> urlString.replace(pattern, replacement)
                }
            Uri.parse(transformedUrl)
        } catch (e: IllegalArgumentException) {
            android.util.Log.e("TransformUrlUseCase", "Error applying rule transformation", e)
            uri
        } catch (e: java.util.regex.PatternSyntaxException) {
            android.util.Log.e("TransformUrlUseCase", "Invalid regex pattern", e)
            uri
        }

    private fun applyAdvancedTransform(
        uri: Uri,
        transformMode: TransformMode.Advanced,
    ): Uri {
        val enabledReplacements =
            transformMode.urlComponentReplacements
                .filter { it.isEnabled }
                .associate { it.component to it.replacement }

        if (enabledReplacements.isEmpty()) {
            return uri
        }

        return buildTransformedUri(uri, enabledReplacements)
    }

    private fun buildTransformedUri(
        uri: Uri,
        enabledReplacements: Map<UrlComponent, String>,
    ): Uri {
        val original = UrlComponents.fromUri(uri)
        return UrlComponents(
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
