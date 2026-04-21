package net.dodiya.signalman.data

import android.net.Uri
import com.google.common.net.InternetDomainName
import java.net.URI

data class UrlComponents(
    val scheme: String = "",
    val host: String = "",
    val domain: String = "",
    val port: String = "",
    val path: String = "",
    val query: String = "",
    val fragment: String = "",
    val userInfo: String = "",
) {
    fun toUri(): Uri {
        val fullHost = if (host.isNotEmpty() && domain.isNotEmpty()) "$host.$domain" else host.ifEmpty { domain }
        val authority = if (port.isNotEmpty()) "$fullHost:$port" else fullHost
        val fullAuthority = if (userInfo.isNotEmpty()) "$userInfo@$authority" else authority
        return Uri
            .Builder()
            .scheme(scheme.ifEmpty { "https" })
            .authority(fullAuthority)
            .path(path)
            .query(query)
            .fragment(fragment)
            .build()
    }

    fun isEmpty() = this == UrlComponents()

    companion object {
        fun fromUri(uri: Uri): UrlComponents {
            val fullHost = uri.host ?: ""

            val (hostPart, domainPart) =
                try {
                    val internetDomain = InternetDomainName.from(fullHost)
                    if (internetDomain.hasParent()) {
                        val topPrivateDomain = internetDomain.topPrivateDomain()
                        val publicSuffixDomain: InternetDomainName? = topPrivateDomain.publicSuffix()
                        val publicSuffix: String = publicSuffixDomain?.toString() ?: ""
                        val suffixWithDot = ".$publicSuffix"
                        val dotIndex: Int = fullHost.indexOf(suffixWithDot)
                        if (dotIndex > 0 && publicSuffix.isNotEmpty()) {
                            Pair(fullHost.substring(0, dotIndex), fullHost.substring(dotIndex + 1))
                        } else {
                            Pair("", fullHost)
                        }
                    } else {
                        Pair("", fullHost)
                    }
                } catch (e: IllegalArgumentException) {
                    android.util.Log.w("UrlComponents", "Failed to parse domain: $fullHost", e)
                    Pair(fullHost, "")
                } catch (e: IllegalStateException) {
                    android.util.Log.w("UrlComponents", "Domain has no parent: $fullHost", e)
                    Pair(fullHost, "")
                }

            return UrlComponents(
                scheme = uri.scheme ?: "",
                host = hostPart,
                domain = domainPart,
                port = if (uri.port != -1) uri.port.toString() else "",
                path = uri.path ?: "",
                query = uri.query ?: "",
                fragment = uri.fragment ?: "",
                userInfo = uri.userInfo ?: "",
            )
        }

        fun parse(urlString: String): UrlComponents? =
            try {
                val javaUri = URI(urlString)
                fromUri(Uri.parse(javaUri.toString()))
            } catch (e: java.net.URISyntaxException) {
                android.util.Log.w("UrlComponents", "Invalid URL syntax: $urlString", e)
                null
            } catch (e: IllegalArgumentException) {
                android.util.Log.w("UrlComponents", "Invalid URL argument: $urlString", e)
                null
            }
    }
}
