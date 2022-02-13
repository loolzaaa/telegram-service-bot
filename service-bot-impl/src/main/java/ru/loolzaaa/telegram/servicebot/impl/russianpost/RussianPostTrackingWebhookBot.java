package ru.loolzaaa.telegram.servicebot.impl.russianpost;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.loolzaaa.telegram.servicebot.core.bot.ServiceWebhookBot;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class RussianPostTrackingWebhookBot extends ServiceWebhookBot<RussianPostTrackingBotUser> {

    public RussianPostTrackingWebhookBot(BotConfiguration<RussianPostTrackingBotUser> configuration, String botPath, String nameVariable, String tokenVariable) {
        super(configuration, botPath, nameVariable, tokenVariable);
        register(new TrackHistoryCommand("track", "Russian Post Tracking", configuration));
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());

        String msgText = update.getMessage().getText();

        String probablyTrackNumber = msgText.split("\\s")[0];
        if (RussianPostTrackingService.validateTrackNumber(probablyTrackNumber)) {
            Long userId = update.getMessage().getFrom().getId();
            List<TrackEntry> trackHistory = configuration.getUserById(userId).getTrackEntries();

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
                message.setText("Ошибка. Попробуйте позже.");
            }

            new Thread(() -> {
                trackHistory.sort(Comparator.comparing(TrackEntry::getLastActivity));
                while (trackHistory.size() > 10) trackHistory.remove(0);
            }).start();
        } else {
            message.setText("Некорректный трек номер");
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void processCallbackQueryUpdate(Update update) {
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
}
