package ru.loolzaaa.telegram.servicebot.core.commands.circleci;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.loolzaaa.telegram.servicebot.core.bot.pojo.Configuration;
import ru.loolzaaa.telegram.servicebot.core.circleci.pojo.Project;
import ru.loolzaaa.telegram.servicebot.core.commands.CommonCommand;

import java.util.List;
import java.util.stream.Collectors;

public class CircleCIResultCommand extends CommonCommand {

    public CircleCIResultCommand(String commandIdentifier, String description, Configuration configuration) {
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
                            List<Project> circleCIProjects = u.getCircleCIProjects();
                            if (circleCIProjects != null) {
                                List<String> slugs = circleCIProjects.stream()
                                        .map(project -> project.getSlug().toLowerCase())
                                        .collect(Collectors.toList());
                                return slugs.contains(conditionSlug.toLowerCase());
                            } else {
                                return false;
                            }
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
