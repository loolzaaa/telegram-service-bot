package ru.loolzaaa.telegram.servicebot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class RussianPostTrackingService {

    private static final Pattern TRACK_NUMBER_FORMAT = Pattern.compile("^[A-Za-z]{2}\\d{9}[A-Za-z]{2}$|^\\d{14}$");

    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(4))
            .followRedirects(HttpClient.Redirect.NEVER).build();

    private static final String API_PATH = "https://www.pochta.ru/tracking?" +
            "p_p_id=trackingPortlet_WAR_portalportlet&" +
            "p_p_lifecycle=2&" +
            "p_p_state=normal&" +
            "p_p_mode=view&" +
            "p_p_resource_id=tracking.get-by-barcodes&" +
            "p_p_cacheability=cacheLevelPage&" +
            "p_p_col_id=column-1&" +
            "p_p_col_count=1";

    public static boolean validateTrackNumber(String number) {
        return TRACK_NUMBER_FORMAT.matcher(number).matches();
    }

    public static String track(String trackNumber) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(ofFormData(trackNumber))
                .uri(URI.create(API_PATH))
                .timeout(Duration.ofSeconds(4))
                .setHeader("Accept", "application/json")
//                .setHeader("Accept-Encoding", "gzip")
                .setHeader("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("User-Agent", "Chrome/74.0.3729.169")
                .build();
        HttpResponse<InputStream> responseStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        JsonNode mainNode = MAPPER.readTree(responseStream.body());
        //System.out.println(mainNode.get("response").get(0).get("trackingItem").toPrettyString());

        JsonNode trackNode = mainNode.get("response").get(0).get("trackingItem").get("trackingHistoryItemList");

        String answer = getAnswerString(trackNode);
        if ("".equals(answer)) return "Нет данных";
        else return answer;

        // TODO: GZIP support
//        InputStream decodedStream = getDecodedInputStream(responseStream);
//        byte[] buffer = new byte[2048];
//        int read = decodedStream.read(buffer, 0, buffer.length);
    }

    private static HttpRequest.BodyPublisher ofFormData(String trackNumber) {
        String builder = URLEncoder.encode("barcodes", StandardCharsets.UTF_8) +
                "=" +
                URLEncoder.encode(trackNumber, StandardCharsets.UTF_8);
        return HttpRequest.BodyPublishers.ofString(builder);
    }

    private static InputStream getDecodedInputStream(HttpResponse<InputStream> httpResponse) throws IOException {
        String encoding = determineContentEncoding(httpResponse);
        switch (encoding) {
            case "":
                return httpResponse.body();
            case "gzip":
                return new GZIPInputStream(httpResponse.body());
            default:
                throw new UnsupportedOperationException("Unexpected Content-Encoding: " + encoding);
        }
    }

    private static String determineContentEncoding(HttpResponse<?> httpResponse) {
        return httpResponse.headers().firstValue("Content-Encoding").orElse("");
    }

    private static String getAnswerString(JsonNode trackNode) {
        StringJoiner stringJoiner = new StringJoiner("\r\n");
        Iterator<JsonNode> trackNodeIterator = trackNode.elements();
        while (trackNodeIterator.hasNext()) {
            JsonNode historyNode = trackNodeIterator.next();
            LocalDateTime date = LocalDateTime.parse(historyNode.get("date").asText(), INPUT_FORMAT);
            String humanStatus = historyNode.get("humanStatus").asText();
            stringJoiner
                    .add(date.format(OUTPUT_FORMAT))
                    .add(humanStatus)
                    .add("");
        }
        return stringJoiner.toString();
    }
}
