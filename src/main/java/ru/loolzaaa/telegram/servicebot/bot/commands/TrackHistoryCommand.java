package ru.loolzaaa.telegram.servicebot.bot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.loolzaaa.telegram.servicebot.bot.pojo.Configuration;
import ru.loolzaaa.telegram.servicebot.bot.pojo.TrackEntry;

import java.util.StringJoiner;

public class TrackHistoryCommand extends CommonCommand {

    public TrackHistoryCommand(String commandIdentifier, String description, Configuration configuration) {
        super(commandIdentifier, description, configuration);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());

        StringJoiner joiner = new StringJoiner("\n");
        for (TrackEntry entry : configuration.getUserById(user.getId()).getTrackHistory()) {
            joiner.add(String.format("%s: %s", entry.getNumber(), entry.getDescription()));
        }
        message.setText(joiner.toString());

        sendAnswer(absSender, message);
    }
}
