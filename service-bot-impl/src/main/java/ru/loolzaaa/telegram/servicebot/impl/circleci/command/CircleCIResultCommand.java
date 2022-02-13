package ru.loolzaaa.telegram.servicebot.impl.circleci.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;
import ru.loolzaaa.telegram.servicebot.core.command.CommonCommand;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.CircleCIBotUser;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.CircleCISubscription;

import java.util.List;
import java.util.stream.Collectors;

public class CircleCIResultCommand extends CommonCommand<CircleCIBotUser> {

    public CircleCIResultCommand(String commandIdentifier, String description, BotConfiguration<CircleCIBotUser> configuration) {
        super(commandIdentifier, description, configuration);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (arguments.length >= 6) {
            final String type = arguments[0];
            final String time = arguments[1];
            final String projectName = arguments[2];
            String projectSlug = arguments[3];
            final String workflowName = arguments[4];
            final String workflowStatus = arguments[5];
            if ("workflow-completed".equals(type)) {
                if (projectSlug.toLowerCase().startsWith("gh")) projectSlug = "github" + projectSlug.substring(2);
                if (projectSlug.toLowerCase().startsWith("bb")) projectSlug = "bitbucket" + projectSlug.substring(2);

                final String conditionSlug = projectSlug;
                configuration.getUsers().stream()
                        .filter(u -> {
                            List<CircleCISubscription> subscriptions = u.getSubscriptions();
                            List<String> slugs = subscriptions.stream()
                                    .map(subscription -> subscription.getSlug().toLowerCase())
                                    .collect(Collectors.toList());
                            return slugs.contains(conditionSlug.toLowerCase());
                        })
                        .forEach(u -> {
                            SendMessage message = new SendMessage();
                            message.setChatId(u.getChatId().toString());
                            message.setText(String.join(" ", arguments));
                            sendAnswer(absSender, message);
                        });
            } else {
                //TODO: add other type support
            }
        }
    }
}
