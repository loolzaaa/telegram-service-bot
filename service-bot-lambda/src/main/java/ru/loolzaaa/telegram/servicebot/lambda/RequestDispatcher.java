package ru.loolzaaa.telegram.servicebot.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;
import ru.loolzaaa.telegram.servicebot.core.bot.ServiceWebhookBot;

public class RequestDispatcher {

    private final ServiceWebhookBot bot;

    private ObjectMapper objectMapper;

    public RequestDispatcher(ServiceWebhookBot bot) {
        this.bot = bot;
    }

    public String dispatch(APIGatewayV2HTTPEvent apiGatewayV2HTTPEvent, Context context) throws JsonProcessingException, TelegramApiValidationException {
        switch (apiGatewayV2HTTPEvent.getRouteKey().toLowerCase()) {
            case "/loolz-bot":
                return dispatchToLoolzBot(context, apiGatewayV2HTTPEvent.getBody());
            case "/circleci":
                return dispatchToCircleCIConverter(context, apiGatewayV2HTTPEvent.getBody());
            default:
                throw new IllegalArgumentException("Unknown route key: " + apiGatewayV2HTTPEvent.getRouteKey());
        }
    }

    public String dispatchToLoolzBot(Context context, String requestBody) throws JsonProcessingException, TelegramApiValidationException {
        Update update = objectMapper.readValue(requestBody, Update.class);
        BotApiMethod<?> method = bot.onWebhookUpdateReceived(update);
        if (method != null) {
            method.validate();
            return objectMapper.writeValueAsString(method);
        } else {
            return "{}";
        }
    }

    public String dispatchToCircleCIConverter(Context context, String requestBody) {
        return null;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
