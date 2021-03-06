package ru.loolzaaa.telegram.servicebot.core.command;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BaseUser;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;

public abstract class CommonCommand<T extends BaseUser> extends BotCommand {

    public static final ThreadLocal<Integer> callbackMessageId = new ThreadLocal<>();

    protected BotConfiguration<T> configuration;

    protected CommonCommand(String commandIdentifier, String description, BotConfiguration<T> configuration) {
        super(commandIdentifier, description);
        this.configuration = configuration;
    }

    protected void sendAnswer(AbsSender absSender, BotApiMethod<?> message) {
        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void setCallbackMessageId(Integer messageId) {
        callbackMessageId.set(messageId);
    }

    public static Integer getCallbackMessageId() {
        return callbackMessageId.get();
    }
}
