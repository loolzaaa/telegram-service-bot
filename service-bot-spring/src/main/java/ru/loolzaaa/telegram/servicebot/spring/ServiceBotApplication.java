package ru.loolzaaa.telegram.servicebot.spring;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BaseConfiguration;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BaseUser;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;
import ru.loolzaaa.telegram.servicebot.impl.binance.BinanceLongPollingBot;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.CircleCIBotConfiguration;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.BotUser;

import java.util.ArrayList;

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
        BaseConfiguration<BaseUser> binanceBotConfiguration = new BaseConfiguration<>(new ArrayList<>(), BaseUser::new);
        BotConfiguration<BotUser> circleCIBotConfiguration = new CircleCIBotConfiguration(new ArrayList<>());

        //Uncomment only ONE long polling bot if not provide bot name and token to constructor
        //telegramBotsApi.registerBot(new CircleCILongPollingBot(circleCIBotConfiguration, null, null));
        telegramBotsApi.registerBot(new BinanceLongPollingBot(binanceBotConfiguration, null, null));

        //Uncomment this for Webhook bot creation. Also, don't forget to provide webhook url
        //SetWebhook setWebhook = SetWebhook.builder().url(webhookUrl).build();
        //telegramBotsApi.registerBot(new ServiceWebhookBot(circleCIBotConfiguration, "service"), setWebhook);
    }
}
