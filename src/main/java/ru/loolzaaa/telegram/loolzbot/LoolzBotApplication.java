package ru.loolzaaa.telegram.loolzbot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.loolzaaa.telegram.loolzbot.bot.LoolzBot;

@SpringBootApplication
public class LoolzBotApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(LoolzBotApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
		botsApi.registerBot(new LoolzBot());
	}
}
