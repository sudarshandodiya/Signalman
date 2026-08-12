package net.dodiya.signalman.domain

import android.net.Uri

/**
 * Removes well-known tracking parameters (utm_*, fbclid, gclid, ...) from a URL's
 * query string before it is routed to a target app. Parameter order is preserved
 * and matching is case-insensitive. Non-tracking parameters are kept as-is.
 */
class CleanUrlUseCase {
    private val trackingParams: Set<String> =
        setOf(
            "utm_source",
            "utm_medium",
            "utm_campaign",
            "utm_term",
            "utm_content",
            "utm_id",
            "utm_source_platform",
            "utm_creative_format",
            "utm_marketing_tactic",
            "fbclid",
            "gclid",
            "gclsrc",
            "msclkid",
            "twclid",
            "igshid",
            "li_fat_id",
            "mc_cid",
            "mc_eid",
            "ref_src",
            "ref_url",
            "hsCtaTracking",
            "_hsenc",
            "_hsmi",
            "vero_id",
            "oly_anon_id",
            "oly_enc_id",
            "_ga",
            "_gl",
            "yclid",
            "dclid",
            "srsltid",
            "wt_mc",
            "rb_clickid",
            "gbraid",
            "wbraid",
            "mkt_tok",
            "epik",
            "cmpid",
            "tblci",
        )

    operator fun invoke(uri: Uri): Uri {
        val query = uri.query ?: return uri
        val pairs = query.split("&")
        val keptPairs =
            pairs.filterNot { pair ->
                val rawKey = pair.substringBefore("=")
                trackingParams.contains(Uri.decode(rawKey).lowercase())
            }
        return if (keptPairs.size == pairs.size) {
            uri
        } else {
            rebuildUri(uri, keptPairs)
        }
    }

    private fun rebuildUri(
        uri: Uri,
        keptPairs: List<String>,
    ): Uri {
        val builder = uri.buildUpon().clearQuery()
        keptPairs.forEach { pair ->
            val separatorIndex = pair.indexOf('=')
            val rawKey = if (separatorIndex >= 0) pair.substring(0, separatorIndex) else pair
            val rawValue = if (separatorIndex >= 0) pair.substring(separatorIndex + 1) else ""
            builder.appendQueryParameter(Uri.decode(rawKey), Uri.decode(rawValue))
        }
        return builder.build()
    }
}
