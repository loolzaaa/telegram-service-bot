package ru.loolzaaa.telegram.servicebot.spring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;
import org.telegram.telegrambots.updatesreceivers.ServerlessWebhook;

import java.util.NoSuchElementException;

@RequiredArgsConstructor
@RestController
public class ServiceBotController {

    private final ServerlessWebhook webhook;

    // Example of webhook url: https://wwww.example.com/callback/example_bot
    //                         |---base external URL---|--^^^^--|botPath
    // callback - required part of URL, because of WebhookUtils.getBotUrl()
    @PostMapping(value = "/callback/{botPath}", consumes = "application/json", produces = "application/json")
    BotApiMethod<?> updateReceived(@PathVariable("botPath") String botPath,
                                   @RequestBody Update update) throws Exception {
        return webhook.updateReceived(botPath, update);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchElementException.class)
    String handleUnknownBotName(NoSuchElementException e) {
        return e.getLocalizedMessage();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(TelegramApiValidationException.class)
    String handleRsposnseValidationError(TelegramApiValidationException e) {
        return e.getLocalizedMessage();
    }
}
