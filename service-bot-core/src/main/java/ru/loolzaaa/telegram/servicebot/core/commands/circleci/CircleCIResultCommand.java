package ru.loolzaaa.telegram.servicebot.core.commands.circleci;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.loolzaaa.telegram.servicebot.core.bot.pojo.Configuration;
import ru.loolzaaa.telegram.servicebot.core.commands.CommonCommand;

public class CircleCIResultCommand extends CommonCommand {

    public CircleCIResultCommand(String commandIdentifier, String description, Configuration configuration) {
        super(commandIdentifier, description, configuration);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (arguments.length >= 6) {
            final String type = arguments[0];
            final String time = arguments[1];
            final String projectId = arguments[2];
            final String projectName = arguments[3];
            final String workflowName = arguments[4];
            final String workflowStatus = arguments[5];

            SendMessage message = new SendMessage();
            message.setChatId("@circleci_results");
            message.setText(String.join(" ", arguments));

            sendAnswer(absSender, message);
        }
    }
}
