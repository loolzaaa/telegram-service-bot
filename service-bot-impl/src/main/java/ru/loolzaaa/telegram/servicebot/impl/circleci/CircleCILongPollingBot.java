package ru.loolzaaa.telegram.servicebot.impl.circleci;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.loolzaaa.telegram.servicebot.core.bot.ServiceLongPollingBot;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;
import ru.loolzaaa.telegram.servicebot.core.command.ClearConfigCommand;
import ru.loolzaaa.telegram.servicebot.impl.circleci.command.CircleCICommand;
import ru.loolzaaa.telegram.servicebot.impl.circleci.command.CircleCIResultCommand;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.CircleCIBotUser;

import java.time.LocalDateTime;
import java.util.List;

public class CircleCILongPollingBot extends ServiceLongPollingBot<CircleCIBotUser> {

    public CircleCILongPollingBot(BotConfiguration<CircleCIBotUser> configuration, String nameVariable, String tokenVariable) {
        super(configuration, nameVariable, tokenVariable);
        register(new ClearConfigCommand<>("clear_config", "Clear bot configuration", configuration));
        register(new CircleCIResultCommand("circleci_result", "CircleCI Webhook", configuration));
        register(new CircleCICommand("circleci", "CircleCI API", configuration));
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        for (Update update : updates) {
            if (update.hasMessage()) {
                User user = update.getMessage().getFrom();
                CircleCIBotUser configUser = configuration.getUserById(user.getId());
                if (configUser != null) {
                    if (LocalDateTime.now().minusHours(24L).isAfter(configUser.getLastActivity())) {
                        configUser.setStatus("default");
                        configUser.getSubscriptions().removeIf(s -> s.getPat() == null || s.getSlug() == null);
                    }
                    if (!"default".equals(configUser.getStatus())) {
                        if (update.getMessage().isCommand() && update.getMessage().getText().startsWith("/break")) {
                            configUser.setStatus("breaking");
                            configUser.getSubscriptions().removeIf(s -> s.getPat() == null || s.getSlug() == null);

                            MessageEntity messageEntity = new MessageEntity("bot_command", 0, "/circleci".length());
                            update.getMessage().setText("/circleci break");
                            update.getMessage().setEntities(List.of(messageEntity));
                        }
                        if ("add-subscription-pat".equals(configUser.getStatus())) {
                            MessageEntity messageEntity = new MessageEntity("bot_command", 0, "/circleci".length());
                            update.getMessage().setText("/circleci pat " + update.getMessage().getText());
                            update.getMessage().setEntities(List.of(messageEntity));
                        } else if ("add-subscription-slug".equals(configUser.getStatus())) {
                            MessageEntity messageEntity = new MessageEntity("bot_command", 0, "/circleci".length());
                            update.getMessage().setText("/circleci slug " + update.getMessage().getText());
                            update.getMessage().setEntities(List.of(messageEntity));
                        } else if ("del-subscription".equals(configUser.getStatus())) {
                            MessageEntity messageEntity = new MessageEntity("bot_command", 0, "/circleci".length());
                            update.getMessage().setText("/circleci del " + update.getMessage().getText());
                            update.getMessage().setEntities(List.of(messageEntity));
                        }
                    }
                }
            }
        }
        super.onUpdatesReceived(updates);
    }

    @Override
    protected void processCallbackQueryUpdate(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();

        MessageEntity messageEntity = new MessageEntity("bot_command", 0, callbackQuery.getData().length());

        Message message = callbackQuery.getMessage();
        message.setFrom(callbackQuery.getFrom());
        message.setText(callbackQuery.getData());
        message.setEntities(List.of(messageEntity));

        update.setMessage(message);
        update.setCallbackQuery(null);
        try {
            execute(new AnswerCallbackQuery(callbackQuery.getId()));
            onUpdateReceived(update);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
