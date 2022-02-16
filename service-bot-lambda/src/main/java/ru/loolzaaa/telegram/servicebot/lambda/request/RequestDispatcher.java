package ru.loolzaaa.telegram.servicebot.lambda.request;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;
import org.telegram.telegrambots.updatesreceivers.ServerlessWebhook;
import ru.loolzaaa.telegram.servicebot.impl.circleci.request.CircleCIRequest;
import ru.loolzaaa.telegram.servicebot.impl.circleci.request.CircleCIRequestToUpdateConverter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class RequestDispatcher {

    private final ServerlessWebhook webhook;

    private ObjectMapper objectMapper;

    public RequestDispatcher(ServerlessWebhook webhook) {
        this.webhook = webhook;
    }

    public String dispatch(String routeKey, APIGatewayV2HTTPEvent apiGatewayV2HTTPEvent, Context context)
            throws JsonProcessingException, TelegramApiValidationException, GeneralSecurityException {
        switch (routeKey.toLowerCase()) {
            case "/circleci-bot":
                return dispatchToBot(routeKey, context, apiGatewayV2HTTPEvent);
            case "/circleci":
                return dispatchToCircleCIConverter(routeKey, context, apiGatewayV2HTTPEvent);
            default:
                throw new IllegalArgumentException("Unknown route key: " + routeKey);
        }
    }

    private String dispatchToBot(String routeKey, Context context, APIGatewayV2HTTPEvent event) throws JsonProcessingException, TelegramApiValidationException {
        Update update = objectMapper.readValue(event.getBody(), Update.class);

        BotApiMethod<?> method = webhook.updateReceived(routeKey, update);
        return method != null ? objectMapper.writeValueAsString(method) : "{}";
    }

    private String dispatchToCircleCIConverter(String routeKey, Context context, APIGatewayV2HTTPEvent event)
            throws JsonProcessingException, TelegramApiValidationException, GeneralSecurityException {
        checkCircleCISign(event.getBody(), event.getHeaders().getOrDefault("circleci-signature", ""));

        CircleCIRequest request = objectMapper.readValue(event.getBody(), CircleCIRequest.class);
        Update update = CircleCIRequestToUpdateConverter.convert(request);

        BotApiMethod<?> method = webhook.updateReceived("/circleci-bot", update);
        return method != null ? objectMapper.writeValueAsString(method) : "{}";
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private void checkCircleCISign(String data, String signHeader) throws NoSuchAlgorithmException, InvalidKeyException {
        String actualHash = Arrays.stream(signHeader.split(","))
                .filter(s -> s.startsWith("v1"))
                .map(s -> s.split("=")[1])
                .findFirst()
                .orElseThrow(() -> new SecurityException("V1 sign not found"));
        SecretKeySpec secretKeySpec = new SecretKeySpec(System.getenv("circleci_key").getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);
        String expectedHash = bytesToHex(mac.doFinal(data.getBytes()));
        if (!expectedHash.equalsIgnoreCase(actualHash)) throw new SecurityException("Sign is incorrect");
    }

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
    private static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }
}
