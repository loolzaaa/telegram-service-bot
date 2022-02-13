package ru.loolzaaa.telegram.servicebot.core.command;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.loolzaaa.telegram.servicebot.core.bot.config.AbstractUser;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;

public abstract class CommonCommand<T extends AbstractUser> extends BotCommand {

    protected BotConfiguration<T> configuration;

    protected CommonCommand(String commandIdentifier, String description, BotConfiguration<T> configuration) {
        super(commandIdentifier, description);
        this.configuration = configuration;
    }

    protected void sendAnswer(AbsSender absSender, SendMessage message) {
        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
