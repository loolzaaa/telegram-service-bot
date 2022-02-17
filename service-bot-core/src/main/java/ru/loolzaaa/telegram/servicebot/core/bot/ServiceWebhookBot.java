package ru.loolzaaa.telegram.servicebot.core.bot;

import org.telegram.telegrambots.extensions.bots.commandbot.TelegramWebhookCommandBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BaseUser;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;
import ru.loolzaaa.telegram.servicebot.core.command.CommonCommand;

import java.time.LocalDateTime;
import java.util.List;

public abstract class ServiceWebhookBot<T extends BaseUser> extends TelegramWebhookCommandBot {

    private String botPath;

    private String nameVariable;
    private String tokenVariable;

    protected BotConfiguration<T> configuration;

    protected ServiceWebhookBot(BotConfiguration<T> configuration, String botPath, String nameVariable, String tokenVariable) {
        this.configuration = configuration;
        this.botPath = botPath;
        this.nameVariable = nameVariable;
        this.tokenVariable = tokenVariable;
    }

    protected ServiceWebhookBot(BotConfiguration<T> configuration, String botPath) {
        this(configuration, botPath, null, null);
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
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
        return super.onWebhookUpdateReceived(update);
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

    protected void processCallbackQueryUpdate(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();

        MessageEntity messageEntity = new MessageEntity(EntityType.BOTCOMMAND, 0, callbackQuery.getData().length());

        Message message = callbackQuery.getMessage();
        message.setFrom(callbackQuery.getFrom());
        message.setText(callbackQuery.getData());
        message.setEntities(List.of(messageEntity));

        update.setMessage(message);
        update.setCallbackQuery(null);
        CommonCommand.setCallbackMessageId(message.getMessageId());
        try {
            execute(new AnswerCallbackQuery(callbackQuery.getId()));
            onWebhookUpdateReceived(update);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotToken() {
        return System.getenv(tokenVariable == null ? "bot_token" : tokenVariable);
    }

    @Override
    public String getBotUsername() {
        return System.getenv(nameVariable == null ? "bot_name" : nameVariable);
    }

    @Override
    public String getBotPath() {
        return botPath;
    }
}
