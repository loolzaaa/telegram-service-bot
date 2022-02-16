package ru.loolzaaa.telegram.servicebot.core.bot;

import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.loolzaaa.telegram.servicebot.core.bot.config.AbstractUser;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;

import java.time.LocalDateTime;
import java.util.List;

public abstract class ServiceLongPollingBot<T extends AbstractUser> extends TelegramLongPollingCommandBot {

    private String nameVariable;
    private String tokenVariable;

    protected BotConfiguration<T> configuration;

    protected ServiceLongPollingBot(BotConfiguration<T> configuration, String nameVariable, String tokenVariable) {
        this.configuration = configuration;
        this.nameVariable = nameVariable;
        this.tokenVariable = tokenVariable;
    }

    protected ServiceLongPollingBot(BotConfiguration<T> configuration) {
        this(configuration, null, null);
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        for (Update update : updates) {
            if (update.hasMessage()) {
                Message message = update.getMessage();
                org.telegram.telegrambots.meta.api.objects.User user = message.getFrom();
                T configUser = configuration.getUserById(user.getId());
                if (configUser == null) {
                    configUser = configuration.addUser(user.getId());
                }
                configUser.setFirstName(user.getFirstName());
                configUser.setUsername(user.getUserName());
                configUser.setChatId(update.getMessage().getChat().getId());
                configUser.setLastActivity(LocalDateTime.now());
            }
        }
        super.onUpdatesReceived(updates);
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            processCallbackQueryUpdate(update);
            return;
        }
        if (!update.hasMessage()) {
            System.err.println(update);
        }
    }

    @Override
    public void processInvalidCommandUpdate(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText("Неизвестная команда");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    protected abstract void processCallbackQueryUpdate(Update update);

    @Override
    public String getBotToken() {
        return System.getenv(tokenVariable == null ? "bot_token" : tokenVariable);
    }

    @Override
    public String getBotUsername() {
        return System.getenv(nameVariable == null ? "bot_name" : nameVariable);
    }
}
