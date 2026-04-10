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
            val hostPart: String
            val domainPart: String

            try {
                val internetDomain = InternetDomainName.from(fullHost)
                if (internetDomain.hasParent()) {
                    val topPrivateDomain = internetDomain.topPrivateDomain()
                    val dotIndex = fullHost.indexOf('.' + topPrivateDomain.publicSuffix())
                    if (dotIndex > 0) {
                        hostPart = fullHost.substring(0, dotIndex)
                        domainPart = fullHost.substring(dotIndex + 1)
                    } else {
                        hostPart = ""
                        domainPart = fullHost
                    }
                } else {
                    hostPart = ""
                    domainPart = fullHost
                }
            } catch (e: Exception) {
                hostPart = fullHost
                domainPart = ""
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
            } catch (e: Exception) {
                null
            }
    }
}
