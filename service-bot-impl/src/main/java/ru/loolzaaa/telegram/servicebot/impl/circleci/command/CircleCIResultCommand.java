package ru.loolzaaa.telegram.servicebot.impl.circleci.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;
import ru.loolzaaa.telegram.servicebot.core.command.CommonCommand;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.CircleCIBotUser;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.CircleCISubscription;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class CircleCIResultCommand extends CommonCommand<CircleCIBotUser> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public CircleCIResultCommand(String commandIdentifier, String description, BotConfiguration<CircleCIBotUser> configuration) {
        super(commandIdentifier, description, configuration);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (arguments.length >= 6) {
            String type = arguments[0];
            LocalDateTime time = LocalDateTime.parse(arguments[1], DATE_TIME_FORMATTER);
            String projectName = arguments[2];
            String projectSlug = arguments[3];
            String workflowName = arguments[4];
            String workflowStatus = arguments[5];
            if (CircleCIEventType.WORKFLOW_COMPLETED.getType().equals(type)) {
                if (projectSlug.toLowerCase().startsWith("gh")) projectSlug = "github" + projectSlug.substring(2);
                if (projectSlug.toLowerCase().startsWith("bb")) projectSlug = "bitbucket" + projectSlug.substring(2);

                final String conditionSlug = projectSlug;
                configuration.getUsers().stream()
                        .filter(u -> {
                            List<CircleCISubscription> subscriptions = u.getSubscriptions();
                            List<String> slugs = subscriptions.stream()
                                    .filter(subscription -> subscription.getSlug() != null)
                                    .map(subscription -> subscription.getSlug().toLowerCase())
                                    .collect(Collectors.toList());
                            return slugs.contains(conditionSlug.toLowerCase());
                        })
                        .forEach(u -> {
                            SendMessage message = new SendMessage();
                            message.setChatId(u.getChatId().toString());
                            message.setText(String.join("\n", projectName, workflowStatus));
                            sendAnswer(absSender, message);
                        });
            } else {
                //TODO: add other type support
            }
        }
    }
}
