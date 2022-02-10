package ru.loolzaaa.telegram.servicebot.spring;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import ru.loolzaaa.telegram.servicebot.core.bot.ServiceLongPollingBot;
import ru.loolzaaa.telegram.servicebot.core.bot.ServiceWebhookBot;
import ru.loolzaaa.telegram.servicebot.core.bot.pojo.Configuration;

@RequiredArgsConstructor
@SpringBootApplication
public class ServiceBotApplication implements CommandLineRunner {

    @Value("${bot.webhookUrl}")
    private String webhookUrl;

    private final TelegramBotsApi telegramBotsApi;

    public static void main(String[] args) {
        SpringApplication.run(ServiceBotApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        SetWebhook setWebhook = SetWebhook.builder().url(webhookUrl).build();
        telegramBotsApi.registerBot(new ServiceLongPollingBot());
        telegramBotsApi.registerBot(new ServiceWebhookBot(new Configuration(), "service"), setWebhook);
    }
}
