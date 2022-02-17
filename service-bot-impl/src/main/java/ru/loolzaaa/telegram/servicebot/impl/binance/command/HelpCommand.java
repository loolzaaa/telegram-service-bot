package ru.loolzaaa.telegram.servicebot.impl.binance.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BaseUser;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;
import ru.loolzaaa.telegram.servicebot.core.command.CommonCommand;

public class HelpCommand extends CommonCommand<BaseUser> {

    public HelpCommand(String commandIdentifier, String description, BotConfiguration<BaseUser> configuration) {
        super(commandIdentifier, description, configuration);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        message.setText("Under Construction");
        sendAnswer(absSender, message);
    }
}
