package ru.loolzaaa.telegram.servicebot.impl.circleci;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.loolzaaa.telegram.servicebot.core.bot.ServiceLongPollingBot;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.BotUser;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.BotUserStatus;
import ru.loolzaaa.telegram.servicebot.impl.helper.BotHelper;

import java.time.LocalDateTime;
import java.util.List;

public class CircleCILongPollingBot extends ServiceLongPollingBot<BotUser> {

    public CircleCILongPollingBot(BotConfiguration<BotUser> configuration, String nameVariable, String tokenVariable) {
        super(configuration, nameVariable, tokenVariable);
        BotHelper.registerAllDefaultCommands(this, configuration);
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        for (Update update : updates) {
            if (update.hasMessage()) {
                if (update.getMessage().isCommand() && update.getMessage().getText().startsWith("/start")) {
                    BotHelper.changeMessageToCommand(update, "/circleci");
                } else if (update.getMessage().isCommand() && update.getMessage().getText().startsWith("/help")) {
                    BotHelper.changeMessageToCommand(update, "/circleci help");
                }

                User user = update.getMessage().getFrom();
                BotUser configUser = configuration.getUserById(user.getId());
                if (configUser != null) {
                    if (LocalDateTime.now().minusHours(24L).isAfter(configUser.getLastActivity())) {
                        configUser.setStatus(BotUserStatus.DEFAULT);
                        configUser.clearUnfinishedSubscriptions();
                    }
                    if (configUser.getStatus() != BotUserStatus.DEFAULT) {
                        if (update.getMessage().isCommand() && update.getMessage().getText().startsWith("/break")) {
                            configUser.setStatus(BotUserStatus.BREAKING);
                            configUser.clearUnfinishedSubscriptions();
                            BotHelper.changeMessageToCommand(update, "/circleci break");
                        }
                        if (configUser.getStatus() == BotUserStatus.ADD_SUBSCRIPTION_PAT) {
                            BotHelper.changeMessageToCommand(update, "/circleci pat " + update.getMessage().getText());
                        } else if (configUser.getStatus() == BotUserStatus.ADD_SUBSCRIPTION_SLUG) {
                            BotHelper.changeMessageToCommand(update, "/circleci slug " + update.getMessage().getText());
                        } else if (configUser.getStatus() == BotUserStatus.DEL_SUBSCRIPTION) {
                            BotHelper.changeMessageToCommand(update, "/circleci del " + update.getMessage().getText());
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

        MessageEntity messageEntity = new MessageEntity(EntityType.BOTCOMMAND, 0, callbackQuery.getData().length());

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
