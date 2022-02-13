package ru.loolzaaa.telegram.servicebot.core.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.loolzaaa.telegram.servicebot.core.bot.config.AbstractUser;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;

public class ClearConfigCommand<T extends AbstractUser> extends CommonCommand<T> {

    public ClearConfigCommand(String commandIdentifier, String description, BotConfiguration<T> configuration) {
        super(commandIdentifier, description, configuration);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());

        if (System.getenv("adm_username").equals(user.getUserName())) {
            configuration.getUsers().clear();
            message.setText("Готово");
        } else {
            message.setText("Нет доступа");
        }

        sendAnswer(absSender, message);
    }
}
