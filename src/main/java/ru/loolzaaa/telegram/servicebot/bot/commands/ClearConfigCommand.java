package ru.loolzaaa.telegram.servicebot.bot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.loolzaaa.telegram.servicebot.bot.pojo.Configuration;

import java.util.ArrayList;

public class ClearConfigCommand extends CommonCommand {

    public ClearConfigCommand(String commandIdentifier, String description, Configuration configuration) {
        super(commandIdentifier, description, configuration);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());

        if (System.getenv("adm_username").equals(user.getUserName())) {
            configuration.setUsers(new ArrayList<>());
            message.setText("Готово");
        } else {
            message.setText("Нет доступа");
        }

        sendAnswer(absSender, message);
    }
}
