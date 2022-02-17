package ru.loolzaaa.telegram.servicebot.impl.binance.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class BinanceCurrencyService {

    private static final String ENDPOINT = "https://api.binance.com/api/v3";

    private static final String CURRENT_AVG_PRICE_API = "/avgPrice";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(4))
            .followRedirects(HttpClient.Redirect.NEVER).build();

    public static String getCurrentAveragePrice(String symbol) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(String.format("%s%s?symbol=%s", ENDPOINT, CURRENT_AVG_PRICE_API, symbol)))
                .timeout(Duration.ofSeconds(4))
                .setHeader("Accept", "application/json")
                .setHeader("User-Agent", "Chrome/74.0.3729.169")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode mainNode = objectMapper.readTree(response.body());

        return mainNode.get("price").asText();
    }
}
