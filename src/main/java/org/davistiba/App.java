package org.davistiba;

import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
// ...existing code...

/**
 * Minified version of sherlock-project
 * Search given Username in 1000 Social Networks.
 * FOR JAVA 11+
 *
 * @author Davis Tibbz
 */
public class App {
        /**
         * Legacy method for unit test compatibility
         */
        public static CompletableFuture<HttpResponse<String>> doSearch(String username, String uri) {
        String finalUri = uri.replace("%", username);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(finalUri))
            .header("User-Agent", USERAGENT)
            .timeout(Duration.ofMillis(3000))
            .GET()
            .build();
        return HttpClient.newHttpClient()
            .sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }
    static final AtomicInteger FOUND = new AtomicInteger(0);
    static final AtomicInteger NOTFOUND = new AtomicInteger(0);
    private static final Gson gson = new Gson();
    private static final ExecutorService executor = Executors.newWorkStealingPool();
    private static final String USERAGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.2 (KHTML, like Gecko) Chrome/22.0.1216.0 Safari/537.2";
    private static final Pattern USERNAME_REGEX = Pattern.compile("^[a-zA-Z0-9_-]{2,}$");
    private static final PrintWriter pw = new PrintWriter(System.out);
    private static final StringBuilder foundList = new StringBuilder();
    private static final StringBuilder notFoundList = new StringBuilder();
    private static final StringBuilder failedList = new StringBuilder();
    private static final Map<String, Double> confidenceMap = new HashMap<>();
    private static final List<Map<String, Object>> jsonResults = new java.util.ArrayList<>();

    public static void main(String[] args) throws Exception {
        if (args.length == 0) throw new IllegalArgumentException("Username arg is missing. Bye!");
        final String username = args[0];

        if (!USERNAME_REGEX.matcher(username).matches()) {
            throw new IllegalArgumentException("Username is invalid");
        }

        //Load json file from 'resources' dir
        URL websiteFile = App.class.getClassLoader().getResource("websites.json");
        assert websiteFile != null;
        Website[] websites = gson.fromJson(new BufferedReader(new InputStreamReader(websiteFile.openStream())), Website[].class);

        long start = System.nanoTime();
        System.out.printf("Loaded %d websites\n", websites.length);
        System.out.printf("Has began at %s \n", LocalDateTime.now());
        System.out.printf("Searching for %s... \n", username);

        Random random = new Random();
        List<CompletableFuture<Void>> cfList = Arrays.stream(websites)
            .map(w -> doSearchWithFingerprint(username, w)
                .thenAcceptAsync(result -> {
                    // Rate limiter + delay acak
                    try { Thread.sleep(500 + random.nextInt(1000)); } catch (InterruptedException ignored) {}
                }, executor))
            .toList();

        CompletableFuture<?>[] mama = cfList.toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(mama).join();

        pw.printf("Time elapsed (ms): %.3f\n", (System.nanoTime() - start) / 1.00E6);
        pw.println("TOTAL FOUND: " + FOUND.get());
        pw.println("TOTAL NOTFOUND: " + NOTFOUND.get());
        pw.println();
        pw.println("Akun yang ditemukan:");
        if (foundList.length() > 0) {
            String[] lines = foundList.toString().split("\\n");
            for (String line : lines) {
                String url = line.replaceFirst("- \\[Akun ditemukan] ", "").trim();
                if (!url.isEmpty()) {
                    Double conf = confidenceMap.getOrDefault(url, 0.0);
                    pw.println(url + " (confidence: " + String.format("%.2f", conf) + ")");
                }
            }
        } else {
            pw.println("- Tidak ada");
        }
        executor.shutdown();
        pw.close();
        executor.awaitTermination(60, TimeUnit.SECONDS);

        // Setelah semua thread selesai, baru tulis file hasil
        String folderName = "hasil";
        String projectRoot = System.getProperty("user.dir");
        java.nio.file.Path hasilDir = java.nio.file.Paths.get(projectRoot, folderName);
        if (!java.nio.file.Files.exists(hasilDir)) {
            java.nio.file.Files.createDirectories(hasilDir);
        }
        String filename = hasilDir.resolve("hasil_pencarian_" + username + ".txt").toString();
        String jsonFile = hasilDir.resolve("hasil_pencarian_" + username + ".json").toString();
        String csvFile = hasilDir.resolve("hasil_pencarian_" + username + ".csv").toString();
        try (PrintWriter fileWriter = new PrintWriter(filename)) {
            fileWriter.println("Hasil pencarian username: " + username);
            fileWriter.println("Tanggal: " + LocalDateTime.now());
            fileWriter.println();
            fileWriter.println("Ditemukan di:");
            fileWriter.print(foundList.length() > 0 ? foundList : "- Tidak ada\n");
            fileWriter.println();
            fileWriter.println("Tidak ditemukan di:");
            fileWriter.print(notFoundList.length() > 0 ? notFoundList : "- Tidak ada\n");
            fileWriter.println();
            fileWriter.println("Gagal diakses:");
            fileWriter.print(failedList.length() > 0 ? failedList : "- Tidak ada\n");
            fileWriter.println();
            fileWriter.println("TOTAL FOUND: " + FOUND.get());
            fileWriter.println("TOTAL NOTFOUND: " + NOTFOUND.get());
            fileWriter.println();
            // Bagian khusus di akhir: Akun yang ditemukan
            fileWriter.println("Akun yang ditemukan:");
            if (foundList.length() > 0) {
                String[] lines = foundList.toString().split("\\n");
                for (String line : lines) {
                    String url = line.replaceFirst("- \\[Akun ditemukan] ", "").trim();
                    if (!url.isEmpty()) {
                        Double conf = confidenceMap.getOrDefault(url, 0.0);
                        fileWriter.println(url + " (confidence: " + String.format("%.2f", conf) + ")");
                    }
                }
            } else {
                fileWriter.println("- Tidak ada");
            }
            fileWriter.flush();
        }
        // Output JSON
        try (PrintWriter jsonWriter = new PrintWriter(jsonFile)) {
            jsonWriter.println(gson.toJson(jsonResults));
        }
        // Output CSV
        try (PrintWriter csvWriter = new PrintWriter(csvFile)) {
            csvWriter.println("url,found,confidence,category");
            for (Map<String, Object> row : jsonResults) {
                csvWriter.printf("%s,%s,%.2f,%s\n",
                        row.get("url"),
                        row.get("found"),
                        row.get("confidence"),
                        row.getOrDefault("category", ""));
            }
        }
    }

    /**
     * Our actual Search method
     *
     * @param username Username
     * @param uri      target address
     * @return response "Promise"
     */
    public static CompletableFuture<Void> doSearchWithFingerprint(String username, Website website) {
        String finalUri = website.getUrl().replace("%", username);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(finalUri))
                .header("User-Agent", USERAGENT)
                .timeout(Duration.ofMillis(3000))
                .GET()
                .build();
        return HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAcceptAsync(response -> {
                    int status = response.statusCode();
                    double confidence = 0.0;
                    boolean found = false;
                    String fingerprint = website.getFingerprint();
                    String category = website.getCategory();
                    if (status == 200 || status == 301 || status == 302) {
                        // HTML fingerprint check
                        try {
                            Document doc = Jsoup.parse(response.body());
                            if (fingerprint != null && !fingerprint.isEmpty()) {
                                if (doc.html().contains(fingerprint)) {
                                    confidence = 1.0;
                                } else {
                                    confidence = 0.7;
                                }
                            } else {
                                confidence = 0.5;
                            }
                        } catch (Exception e) {
                            confidence = 0.5;
                        }
                        found = true;
                        confidenceMap.put(finalUri, confidence);
                        foundList.append("- [Akun ditemukan] ").append(finalUri).append("\n");
                    } else if (status == 404) {
                        found = false;
                        notFoundList.append("- ").append(finalUri).append("\n");
                    } else {
                        failedList.append("- ").append(finalUri).append("\n");
                    }
                    Map<String, Object> resultRow = new HashMap<>();
                    resultRow.put("url", finalUri);
                    resultRow.put("found", found);
                    resultRow.put("confidence", confidence);
                    resultRow.put("category", category);
                    jsonResults.add(resultRow);
                }, executor);
    }

    /**
     * Print result of given URL lookup, then update count atomically
     * @param result Http status code
     * @param url the target endpoint
     */
    public static void handleResult(int result, String url) {
        // ...existing code...
        // Deprecated: use handleResultWithCollect
        handleResultWithCollect(result, url);
    }

    /**
     * Print result and collect for file output
     */
    public static void handleResultWithCollect(int result, String url) {
        switch (result) {
            case 301, 302, 200 -> {
                pw.printf("\u001B[32m✓ DITEMUKAN: %s\u001B[0m\n", url);
                FOUND.incrementAndGet();
                foundList.append("- [Akun ditemukan] ").append(url).append("\n");
            }
            case 404 -> {
                pw.printf("\u001B[31mx TIDAK DITEMUKAN: %s\u001B[0m\n", url);
                NOTFOUND.incrementAndGet();
                notFoundList.append("- ").append(url).append("\n");
            }
            default -> {
                System.out.printf("FAILED at %s \n", url);
                failedList.append("- ").append(url).append("\n");
            }
        }
    }

}
