package org.davistiba;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.net.http.HttpResponse;

public class AppMockTest {
    @Test
    public void testDoSearchWithFingerprint_Mock200() throws IOException, InterruptedException, ExecutionException {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(200).setBody("<html><body>profile found</body></html>"));
        server.start();
        Website w = new WebsiteMock(server.url("/profile/%").toString(), "profile found", "social");
        App.doSearchWithFingerprint("testuser", w).get();
        server.shutdown();
        // You can add assertions for foundList, confidenceMap, etc.
        Assertions.assertTrue(App.confidenceMap.values().stream().anyMatch(c -> c == 1.0));
    }

    @Test
    public void testDoSearchWithFingerprint_Mock404() throws IOException, InterruptedException, ExecutionException {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(404));
        server.start();
        Website w = new WebsiteMock(server.url("/profile/%").toString(), "", "social");
        App.doSearchWithFingerprint("testuser", w).get();
        server.shutdown();
        Assertions.assertTrue(App.notFoundList.length() > 0);
    }

    // WebsiteMock class for testing
    static class WebsiteMock extends Website {
        private final String url;
        private final String fingerprint;
        private final String category;
        public WebsiteMock(String url, String fingerprint, String category) {
            this.url = url;
            this.fingerprint = fingerprint;
            this.category = category;
        }
        @Override
        public String getUrl() { return url; }
        @Override
        public String getFingerprint() { return fingerprint; }
        @Override
        public String getCategory() { return category; }
    }
}
