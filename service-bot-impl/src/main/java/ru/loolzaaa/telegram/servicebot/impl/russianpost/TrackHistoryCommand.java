package ru.loolzaaa.telegram.servicebot.impl.russianpost;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;
import ru.loolzaaa.telegram.servicebot.core.command.CommonCommand;

import java.util.StringJoiner;

public class TrackHistoryCommand extends CommonCommand<RussianPostTrackingBotUser> {

    public TrackHistoryCommand(String commandIdentifier, String description, BotConfiguration<RussianPostTrackingBotUser> configuration) {
        super(commandIdentifier, description, configuration);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());

        StringJoiner joiner = new StringJoiner("\n");
        for (TrackEntry entry : configuration.getUserById(user.getId()).getTrackEntries()) {
            joiner.add(String.format("%s: %s", entry.getNumber(), entry.getDescription()));
        }
        message.setText(joiner.toString());

        sendAnswer(absSender, message);
    }
}
