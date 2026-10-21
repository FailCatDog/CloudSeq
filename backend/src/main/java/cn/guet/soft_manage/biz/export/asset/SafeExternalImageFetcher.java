package cn.guet.soft_manage.biz.export.asset;

import cn.guet.soft_manage.biz.export.dto.ResolvedExportAsset;
import cn.guet.soft_manage.biz.export.util.ImageMediaTypes;
import cn.guet.soft_manage.frame.config.ExportProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Downloads public http(s) images into memory for export embedding (SSRF-safe).
 */
@Component
public class SafeExternalImageFetcher {

    private static final Logger log = LoggerFactory.getLogger(SafeExternalImageFetcher.class);
    private static final String USER_AGENT = "CloudSeq-Export/1.0";
    private static final int MAX_REDIRECTS = 3;

    @Resource
    private ExportProperties properties;

    private HttpClient httpClient;

    @PostConstruct
    void init() {
        this.httpClient = buildHttpClient();
    }

    /** Unit-test wiring outside Spring. */
    void bindForTest(ExportProperties properties) {
        this.properties = properties;
        this.httpClient = buildHttpClient();
    }

    private HttpClient buildHttpClient() {
        return HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofMillis(Math.max(1, properties.getExternalImageTimeoutMs())))
            .build();
    }

    public ResolvedExportAsset fetch(String imageUrl) {
        if (!properties.isExternalImageEnabled()) {
            return null;
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        try {
            URI uri = URI.create(imageUrl.trim());
            byte[] bytes = downloadWithRedirects(uri, 0);
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            String mediaType = ImageMediaTypes.sniff(bytes, null);
            if (mediaType == null) {
                log.warn("export external image: unsupported bytes url={}", imageUrl);
                return null;
            }
            String fileName = fileNameFromUri(uri);
            return ResolvedExportAsset.builder()
                .bytes(bytes)
                .contentType(mediaType)
                .fileName(fileName)
                .build();
        } catch (IllegalArgumentException ex) {
            log.warn("export external image: bad url={}", imageUrl);
            return null;
        } catch (Exception ex) {
            log.warn("export external image: fetch failed url={} reason={}", imageUrl, ex.getMessage());
            return null;
        }
    }

    private byte[] downloadWithRedirects(URI uri, int depth) throws Exception {
        if (depth > MAX_REDIRECTS) {
            return null;
        }
        if (!isUriAllowed(uri)) {
            log.warn("export external image: forbidden uri={}", uri);
            return null;
        }

        Duration timeout = Duration.ofMillis(Math.max(1, properties.getExternalImageTimeoutMs()));
        HttpRequest request = HttpRequest.newBuilder(uri)
            .timeout(timeout)
            .header("User-Agent", USER_AGENT)
            .GET()
            .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        int status = response.statusCode();
        if (status >= 300 && status < 400) {
            String location = response.headers().firstValue("Location").orElse(null);
            try (InputStream ignored = response.body()) {
                // drain
            }
            if (location == null || location.isBlank()) {
                return null;
            }
            URI next = uri.resolve(location);
            return downloadWithRedirects(next, depth + 1);
        }
        if (status < 200 || status >= 300) {
            try (InputStream ignored = response.body()) {
                // drain
            }
            return null;
        }

        long maxBytes = properties.getExternalImageMaxBytes();
        try (InputStream in = response.body();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            long total = 0;
            int n;
            while ((n = in.read(buf)) >= 0) {
                total += n;
                if (total > maxBytes) {
                    log.warn("export external image: exceeds max bytes url={}", uri);
                    return null;
                }
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        }
    }

    /** Production SSRF checks; overridden in unit tests for loopback HttpServer. */
    protected boolean isUriAllowed(URI uri) {
        if (PrivateAddressChecker.isForbiddenUri(uri)) {
            return false;
        }
        return !PrivateAddressChecker.isForbiddenHost(uri.getHost());
    }

    private static String fileNameFromUri(URI uri) {
        String path = uri.getPath();
        if (path == null || path.isBlank()) {
            return "image";
        }
        int slash = path.lastIndexOf('/');
        String name = slash >= 0 ? path.substring(slash + 1) : path;
        return name.isBlank() ? "image" : name;
    }
}
