package ru.loolzaaa.telegram.servicebot.bot.commands;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.loolzaaa.telegram.servicebot.bot.pojo.Configuration;

public abstract class CommonCommand extends BotCommand {

    protected Configuration configuration;

    protected CommonCommand(String commandIdentifier, String description, Configuration configuration) {
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
