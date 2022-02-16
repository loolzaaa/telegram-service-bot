package ru.loolzaaa.telegram.servicebot.impl.circleci;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.loolzaaa.telegram.servicebot.core.bot.ServiceWebhookBot;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.BotUser;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.BotUserStatus;
import ru.loolzaaa.telegram.servicebot.impl.circleci.helper.BotHelper;
import ru.loolzaaa.telegram.servicebot.impl.circleci.helper.I18n;

import java.time.LocalDateTime;

public class CircleCIWebhookBot extends ServiceWebhookBot<BotUser> {

    public CircleCIWebhookBot(BotConfiguration<BotUser> configuration, String botPath, String nameVariable, String tokenVariable) {
        super(configuration, botPath, nameVariable, tokenVariable);
        BotHelper.registerAllDefaultCommands(this, configuration);
    }

    public CircleCIWebhookBot(BotConfiguration<BotUser> configuration, String botPath) {
        this(configuration, botPath, "circleci_bot_name",  "circleci_bot_token");
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().isCommand() && update.getMessage().getText().startsWith("/start")) {
                BotHelper.changeMessageToCommand(update, "/circleci");
            } else if (update.getMessage().isCommand() && update.getMessage().getText().startsWith("/help")) {
                BotHelper.changeMessageToCommand(update, "/circleci help");
            }

            User user = update.getMessage().getFrom();
            BotUser configUser = configuration.getUserById(user.getId());
            if (configUser != null) {
                I18n.setCurrentBundle(configUser.getLocaleKey());
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
        return super.onWebhookUpdateReceived(update);
    }
}
