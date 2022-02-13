package ru.loolzaaa.telegram.servicebot.impl.circleci;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.loolzaaa.telegram.servicebot.core.bot.ServiceWebhookBot;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;
import ru.loolzaaa.telegram.servicebot.core.command.ClearConfigCommand;

import java.util.List;

public class CircleCILongWebhookBot extends ServiceWebhookBot<CircleCIBotUser> {

    public CircleCILongWebhookBot(BotConfiguration<CircleCIBotUser> configuration, String botPath, String nameVariable, String tokenVariable) {
        super(configuration, botPath, nameVariable, tokenVariable);
        register(new ClearConfigCommand<>("clear_config", "Clear bot configuration", configuration));
        register(new CircleCIResultCommand("circleci_result", "CircleCI Webhook", configuration));
        register(new CircleCICommand("circleci", "CircleCI API", configuration));
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
