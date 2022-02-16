package ru.loolzaaa.telegram.servicebot.spring;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;
import ru.loolzaaa.telegram.servicebot.impl.circleci.CircleCILongPollingBot;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.CircleCIBotConfiguration;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.BotUser;

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
        BotConfiguration<BotUser> configuration = new CircleCIBotConfiguration(BotUser::new);
        //SetWebhook setWebhook = SetWebhook.builder().url(webhookUrl).build();
        telegramBotsApi.registerBot(new CircleCILongPollingBot(configuration));
        //telegramBotsApi.registerBot(new ServiceWebhookBot(configuration, "service"), setWebhook);
    }
}
