package net.dodiya.signalman.data

import android.net.Uri

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

    fun isEmpty(): Boolean =
        scheme.isEmpty() &&
            host.isEmpty() &&
            domain.isEmpty() &&
            port.isEmpty() &&
            path.isEmpty() &&
            query.isEmpty() &&
            fragment.isEmpty() &&
            userInfo.isEmpty()

    companion object {
        fun fromUri(uri: Uri): UrlComponents {
            val fullHost = uri.host ?: ""
            val lastDotIndex = fullHost.lastIndexOf('.')
            val hostPart: String
            val domainPart: String
            if (lastDotIndex != -1) {
                hostPart = fullHost.substring(0, lastDotIndex)
                domainPart = fullHost.substring(lastDotIndex + 1)
            } else {
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
                fromUri(Uri.parse(urlString))
            } catch (e: Exception) {
                null
            }
    }
}

data class UrlComponentReplacement(
    val component: UrlComponent,
    val replacement: String,
    val isEnabled: Boolean = true,
)

enum class UrlComponent(
    val displayName: String,
    val description: String,
) {
    SCHEME("Scheme", "e.g., https, http, ftp"),
    HOST("Host", "e.g., google, youtube"),
    DOMAIN("Domain", "e.g., com, net, org"),
    PORT("Port", "e.g., 8080, 443"),
    PATH("Path", "e.g., /watch, /videos/123"),
    QUERY("Query", "e.g., v=abc123&t=60"),
    FRAGMENT("Fragment", "e.g., section1, timestamp"),
    USER_INFO("User Info", "e.g., user:password"),
}
