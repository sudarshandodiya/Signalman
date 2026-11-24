package net.dodiya.signalman.ui

private val URL_PATTERN = Regex("""(?i)https?://[^\s<>"']+""")
private val DOMAIN_PATTERN = Regex("""(?i)(?:www\.)?[a-z0-9-]+(?:\.[a-z0-9-]+)+(?:[/?#][^\s<>"']*)?""")

/**
 * Extracts the first HTTP(S) URL from text shared by another app.
 *
 * Supports a plain URL, a URL inside a sentence, and a scheme-less domain such as
 * `example.com/path`. Returns null when the shared text contains no usable URL.
 */
fun extractSharedUrl(text: String?): String? {
    val match =
        text
            ?.takeIf { it.isNotBlank() }
            ?.let { value ->
                URL_PATTERN.find(value)?.value
                    ?: DOMAIN_PATTERN.find(value)?.value?.let { "https://$it" }
            }

    // Trailing sentence punctuation is not part of the URL.
    return match?.trimEnd(',', '.', ';', '!', '?', ')', ']', '}')
}
