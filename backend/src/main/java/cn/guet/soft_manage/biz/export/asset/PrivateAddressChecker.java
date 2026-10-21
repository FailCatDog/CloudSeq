package cn.guet.soft_manage.biz.export.asset;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * SSRF guard: reject non-http(s) schemes, localhost, and private / link-local addresses.
 */
public final class PrivateAddressChecker {

    private PrivateAddressChecker() {
    }

    public static boolean isForbiddenUri(URI uri) {
        if (uri == null) {
            return true;
        }
        String scheme = uri.getScheme();
        if (scheme == null) {
            return true;
        }
        String lower = scheme.toLowerCase();
        if (!"http".equals(lower) && !"https".equals(lower)) {
            return true;
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            return true;
        }
        if ("localhost".equalsIgnoreCase(host)) {
            return true;
        }
        // Literal IP in host — validate without DNS
        if (looksLikeIpLiteral(host)) {
            try {
                return isForbiddenAddress(InetAddress.getByName(host));
            } catch (UnknownHostException ex) {
                return true;
            }
        }
        return false;
    }

    public static boolean isForbiddenHost(String host) {
        if (host == null || host.isBlank()) {
            return true;
        }
        if ("localhost".equalsIgnoreCase(host)) {
            return true;
        }
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress address : addresses) {
                if (isForbiddenAddress(address)) {
                    return true;
                }
            }
            return false;
        } catch (UnknownHostException ex) {
            return true;
        }
    }

    public static boolean isForbiddenAddress(InetAddress address) {
        if (address == null) {
            return true;
        }
        if (address.isAnyLocalAddress()
            || address.isLoopbackAddress()
            || address.isLinkLocalAddress()
            || address.isSiteLocalAddress()
            || address.isMulticastAddress()) {
            return true;
        }
        // Explicit metadata / CGNAT ranges not always covered by isSiteLocalAddress
        byte[] raw = address.getAddress();
        if (raw.length == 4) {
            int b0 = raw[0] & 0xFF;
            int b1 = raw[1] & 0xFF;
            // 169.254.0.0/16 link-local (also isLinkLocalAddress)
            if (b0 == 169 && b1 == 254) {
                return true;
            }
            // 100.64.0.0/10 shared address space
            if (b0 == 100 && b1 >= 64 && b1 <= 127) {
                return true;
            }
            // 0.0.0.0/8
            if (b0 == 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean looksLikeIpLiteral(String host) {
        if (host.indexOf(':') >= 0) {
            return true; // IPv6
        }
        for (int i = 0; i < host.length(); i++) {
            char c = host.charAt(i);
            if (!(c == '.' || (c >= '0' && c <= '9'))) {
                return false;
            }
        }
        return host.indexOf('.') >= 0;
    }
}
