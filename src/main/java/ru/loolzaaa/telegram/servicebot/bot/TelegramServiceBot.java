package ru.loolzaaa.telegram.servicebot.bot;

import org.telegram.telegrambots.extensions.bots.commandbot.TelegramWebhookCommandBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.loolzaaa.telegram.servicebot.bot.commands.ClearConfigCommand;
import ru.loolzaaa.telegram.servicebot.bot.commands.StartCommand;
import ru.loolzaaa.telegram.servicebot.bot.commands.TrackHistoryCommand;
import ru.loolzaaa.telegram.servicebot.bot.commands.currencies.CurrencyRatesCommand;
import ru.loolzaaa.telegram.servicebot.bot.commands.currencies.RatesInlineMenuCommand;
import ru.loolzaaa.telegram.servicebot.bot.pojo.Configuration;
import ru.loolzaaa.telegram.servicebot.bot.pojo.TrackEntry;
import ru.loolzaaa.telegram.servicebot.bot.pojo.User;
import ru.loolzaaa.telegram.servicebot.service.RussianPostTrackingService;

import java.time.LocalDateTime;
import java.util.List;

public class TelegramServiceBot extends TelegramWebhookCommandBot {

    private Configuration configuration;

    public void init(Configuration configuration) {
        this.configuration = configuration;
        register(new StartCommand("start", "Старт", configuration));
        register(new RatesInlineMenuCommand("rates", "Меню курсов валют", configuration));
        register(new CurrencyRatesCommand("rate", "Курс валют", configuration));
        register(new TrackHistoryCommand("trackhistory", "История отслеживаний", configuration));
        register(new ClearConfigCommand("clearconfig", "", configuration));
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            org.telegram.telegrambots.meta.api.objects.User user = message.getFrom();
            if (configuration.getUserById(user.getId()) == null) {
                configuration.getUsers().add(new User(user.getId()));
            }
            configuration.getUserById(user.getId()).setLastActivity(LocalDateTime.now());
        }
        return super.onWebhookUpdateReceived(update);
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            processCallbackQueryUpdate(update);
            return;
        }

        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());

        String msgText = update.getMessage().getText();

        String probablyTrackNumber = msgText.split("\\s")[0];
        if (RussianPostTrackingService.validateTrackNumber(probablyTrackNumber)) {
            Long userId = update.getMessage().getFrom().getId();
            List<TrackEntry> trackHistory = configuration.getUserById(userId).getTrackHistory();

            String trackNumber = probablyTrackNumber.toUpperCase();
            String description = null;
            try {
                int firstSpaceIndex = msgText.indexOf(" ");
                if (firstSpaceIndex != -1) {
                    description = msgText.substring(msgText.indexOf(" ") + 1).trim();
                    if (description.length() > 128) description = description.substring(0, 128);
                    if ("".equals(description)) description = null;
                }
            } catch (Exception ignored) {}

            TrackEntry entry = new TrackEntry();
            entry.setNumber(trackNumber);
            entry.setLastActivity(LocalDateTime.now());
            entry.setDescription(description);

            if (!trackHistory.contains(entry)) {
                trackHistory.add(entry);
            } else {
                for (TrackEntry e : trackHistory) {
                    if (e.equals(entry)) {
                        if (description != null) e.setDescription(description);
                        description = e.getDescription();
                        e.setLastActivity(LocalDateTime.now());
                    }
                }
            }
            if (description == null) {
                description = "Описание не задано. Можно задать при запросе, отделив пробелом";
            }
            message.setText(String.format("Номер отслеживания: %s\n%s\nПодождите...", trackNumber, description));
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            try {
                String answer = RussianPostTrackingService.track(trackNumber);
                message.setText(answer);
            } catch (Exception e) {
                e.printStackTrace();
                message.setText("Ошибка");
            }
        } else {
            message.setText("Некорректный трек номер");
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void processCallbackQueryUpdate(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();

        MessageEntity messageEntity = new MessageEntity("bot_command", 0, callbackQuery.getData().length());
        messageEntity.setText(callbackQuery.getData());

        Message message = callbackQuery.getMessage();
        message.setText(callbackQuery.getData());
        message.setEntities(List.of(messageEntity));

        update.setMessage(message);
        update.setCallbackQuery(null);
        try {
            execute(new AnswerCallbackQuery(callbackQuery.getId()));
            onWebhookUpdateReceived(update);
        } catch (TelegramApiException e) {
            e.printStackTrace();
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

    @Override
    public String getBotToken() {
        return System.getenv("bot_token");
    }

    @Override
    public String getBotUsername() {
        return System.getenv("bot_name");
    }

    @Override
    public String getBotPath() {
        return null;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
