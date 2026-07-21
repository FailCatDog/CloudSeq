package cn.guet.soft_manage.biz.export.asset;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrivateAddressCheckerTest {

    @Test
    void rejectsLocalhostLiteral() {
        assertTrue(PrivateAddressChecker.isForbiddenUri(URI.create("http://localhost/a.png")));
        assertTrue(PrivateAddressChecker.isForbiddenUri(URI.create("http://LOCALHOST/a.png")));
    }

    @Test
    void rejectsLoopbackAndPrivateIps() throws Exception {
        assertTrue(PrivateAddressChecker.isForbiddenAddress(InetAddress.getByName("127.0.0.1")));
        assertTrue(PrivateAddressChecker.isForbiddenAddress(InetAddress.getByName("10.0.0.1")));
        assertTrue(PrivateAddressChecker.isForbiddenAddress(InetAddress.getByName("172.16.5.1")));
        assertTrue(PrivateAddressChecker.isForbiddenAddress(InetAddress.getByName("192.168.1.1")));
        assertTrue(PrivateAddressChecker.isForbiddenAddress(InetAddress.getByName("169.254.169.254")));
    }

    @Test
    void allowsPublicLiteralIp() throws Exception {
        assertFalse(PrivateAddressChecker.isForbiddenAddress(InetAddress.getByName("8.8.8.8")));
    }

    @Test
    void rejectsNonHttpSchemes() {
        assertTrue(PrivateAddressChecker.isForbiddenUri(URI.create("file:///tmp/x.png")));
        assertTrue(PrivateAddressChecker.isForbiddenUri(URI.create("ftp://example.com/a.png")));
    }

    @Test
    void allowsHttpHttpsWithPublicHostName() {
        // host name alone is not forbidden; DNS check happens separately
        assertFalse(PrivateAddressChecker.isForbiddenUri(URI.create("https://pdai.tech/images/a.png")));
        assertFalse(PrivateAddressChecker.isForbiddenUri(URI.create("http://example.com/a.png")));
    }

    @Test
    void rejectsLiteralPrivateIpv4InUri() throws UnknownHostException {
        assertTrue(PrivateAddressChecker.isForbiddenUri(URI.create("http://127.0.0.1/x.png")));
        assertTrue(PrivateAddressChecker.isForbiddenUri(URI.create("http://192.168.0.2/x.png")));
    }
}
