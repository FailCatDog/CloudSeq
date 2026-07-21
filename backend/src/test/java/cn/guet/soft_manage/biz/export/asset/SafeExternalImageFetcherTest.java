package cn.guet.soft_manage.biz.export.asset;

import cn.guet.soft_manage.biz.export.dto.ResolvedExportAsset;
import cn.guet.soft_manage.frame.config.ExportProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SafeExternalImageFetcherTest {

    private static final byte[] TINY_PNG = new byte[]{
        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
        0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
        0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
        0x08, 0x02, 0x00, 0x00, 0x00, (byte) 0x90, 0x77, 0x53,
        (byte) 0xDE, 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41,
        0x54, 0x08, (byte) 0xD7, 0x63, (byte) 0xF8, (byte) 0xCF, (byte) 0xC0, 0x00,
        0x00, 0x00, 0x03, 0x00, 0x01, 0x00, 0x05, (byte) 0xFE,
        (byte) 0xD4, (byte) 0xEF, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45,
        0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
    };

    private HttpServer server;
    private String baseUrl;
    private SafeExternalImageFetcher fetcher;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/ok.png", exchange -> {
            byte[] body = TINY_PNG;
            exchange.getResponseHeaders().add("Content-Type", "image/png");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.createContext("/not-image", exchange -> {
            byte[] body = "hello".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.createContext("/too-big", exchange -> {
            byte[] body = new byte[64];
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();

        fetcher = new SafeExternalImageFetcher();
        fetcher.bindForTest(new ExportProperties());
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void rejectsLoopbackEvenIfServerIsLocal() {
        assertNull(fetcher.fetch(baseUrl + "/ok.png"));
    }

    @Test
    void rejectsLocalhostLiteral() {
        assertNull(fetcher.fetch("http://localhost/ok.png"));
    }

    @Test
    void rejectsMetadataIp() {
        assertNull(fetcher.fetch("http://169.254.169.254/latest/meta-data/"));
    }

    @Test
    void returnsNullWhenDisabled() {
        ExportProperties props = new ExportProperties();
        props.setExternalImageEnabled(false);
        SafeExternalImageFetcher disabled = new SafeExternalImageFetcher();
        disabled.bindForTest(props);
        assertNull(disabled.fetch("https://example.com/a.png"));
    }

    @Test
    void downloadsPngWhenUriAllowedForTest() {
        ExportProperties props = new ExportProperties();
        SafeExternalImageFetcher local = new SafeExternalImageFetcher() {
            @Override
            protected boolean isUriAllowed(URI uri) {
                return "http".equalsIgnoreCase(uri.getScheme())
                    && "127.0.0.1".equals(uri.getHost());
            }
        };
        local.bindForTest(props);
        ResolvedExportAsset got = local.fetch(baseUrl + "/ok.png");
        assertNotNull(got);
        assertEquals("image/png", got.getContentType());
        assertEquals(TINY_PNG.length, got.getBytes().length);
    }

    @Test
    void rejectsNonImageBytes() {
        SafeExternalImageFetcher local = new SafeExternalImageFetcher() {
            @Override
            protected boolean isUriAllowed(URI uri) {
                return true;
            }
        };
        local.bindForTest(new ExportProperties());
        assertNull(local.fetch(baseUrl + "/not-image"));
    }

    @Test
    void rejectsOversizedBody() {
        ExportProperties props = new ExportProperties();
        props.setExternalImageMaxBytes(16);
        SafeExternalImageFetcher local = new SafeExternalImageFetcher() {
            @Override
            protected boolean isUriAllowed(URI uri) {
                return true;
            }
        };
        local.bindForTest(props);
        assertNull(local.fetch(baseUrl + "/too-big"));
    }
}
