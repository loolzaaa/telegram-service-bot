package ru.loolzaaa.telegram.servicebot.impl.circleci.command;

import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;
import ru.loolzaaa.telegram.servicebot.core.command.CommonCommand;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.BotUser;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.Subscription;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class CircleCIResultCommand extends CommonCommand<BotUser> {

    private static final String CIRCLECI_RESULTKEY = System.getenv("circleci_resultKey");

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public CircleCIResultCommand(String commandIdentifier, String description, BotConfiguration<BotUser> configuration) {
        super(commandIdentifier, description, configuration);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (arguments.length >= 7 && arguments[0].equals(CIRCLECI_RESULTKEY)) {
            String type = arguments[1];
            LocalDateTime time = LocalDateTime.parse(arguments[2], DATE_TIME_FORMATTER);
            String projectName = arguments[3];
            String projectSlug = arguments[4];
            String workflowName = arguments[5];
            String workflowStatus = arguments[6];
            if (EventType.WORKFLOW_COMPLETED.getType().equals(type)) {
                if (projectSlug.toLowerCase().startsWith("gh")) projectSlug = "github" + projectSlug.substring(2);
                if (projectSlug.toLowerCase().startsWith("bb")) projectSlug = "bitbucket" + projectSlug.substring(2);

                final String conditionSlug = projectSlug;
                List<BotUser> users = configuration.getUsers().stream()
                        .filter(u -> {
                            List<Subscription> subscriptions = u.getSubscriptions();
                            List<String> slugs = subscriptions.stream()
                                    .filter(subscription -> subscription.getSlug() != null)
                                    .map(subscription -> subscription.getSlug().toLowerCase())
                                    .collect(Collectors.toList());
                            return slugs.contains(conditionSlug.toLowerCase());
                        })
                        .collect(Collectors.toList());
                for (BotUser u : users) {
                    SendPhoto resultMessage = SendPhoto.builder()
                            .chatId(u.getChatId().toString())
                            .photo(new InputFile("https://circleci.com/docs/assets/img/docs/svg-passed.png"))
                            .caption(projectName)
                            .build();
                    try {
                        absSender.execute(resultMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                //TODO: add other type support
            }
        }
    }
}
