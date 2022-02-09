package ru.loolzaaa.telegram.servicebot.spring;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.loolzaaa.telegram.servicebot.core.bot.ServiceLongPollingBot;

@SpringBootApplication
public class ServiceBotApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ServiceBotApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new ServiceLongPollingBot());
    }
}
