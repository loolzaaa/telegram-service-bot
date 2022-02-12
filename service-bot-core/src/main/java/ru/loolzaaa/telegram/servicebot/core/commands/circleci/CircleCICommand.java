package ru.loolzaaa.telegram.servicebot.core.commands.circleci;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.loolzaaa.telegram.servicebot.core.bot.pojo.Configuration;
import ru.loolzaaa.telegram.servicebot.core.circleci.pojo.Project;
import ru.loolzaaa.telegram.servicebot.core.commands.CommonCommand;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class CircleCICommand extends CommonCommand {

    private static final String API_PATH = "https://circleci.com/api/v2";

    public CircleCICommand(String commandIdentifier, String description, Configuration configuration) {
        super(commandIdentifier, description, configuration);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (arguments.length >= 1) {
            final String subCommand = arguments[0];
            final String pat;
            String slug;
            ru.loolzaaa.telegram.servicebot.core.bot.pojo.User configUser = configuration.getUserById(user.getId());

            if ("add".equalsIgnoreCase(subCommand) && arguments.length >= 3) {
                pat = arguments[1];
                slug = arguments[2];
                if (slug.toLowerCase().startsWith("gh")) slug = "github" + slug.substring(2);
                if (slug.toLowerCase().startsWith("bb")) slug = "bitbucket" + slug.substring(2);
                List<String> slugs = configUser.getCircleCIProjects().stream()
                        .map(project -> project.getSlug().toLowerCase())
                        .collect(Collectors.toList());
                if (slugs.contains(slug.toLowerCase())) {
                    sendTextAnswer(absSender, chat, "Данный проект уже содержится в листе подписок");
                    return;
                }

                HttpRequest request = HttpRequest.newBuilder(URI.create(API_PATH + "/me"))
                        .header("Circle-Token", pat)
                        .GET()
                        .build();
                try {
                    int code = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
                    if (code == 200) {
                        request = HttpRequest.newBuilder(URI.create(API_PATH + "/project/" + slug))
                                .header("Circle-Token", pat)
                                .GET()
                                .build();
                        HttpResponse<String> projectResponse = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                        if (projectResponse.statusCode() == 200) {
                            JsonNode projectNode = new ObjectMapper().readTree(projectResponse.body());
                            final String projectName = projectNode.get("name").asText();
                            Project project = new Project();
                            project.setName(projectName);
                            project.setSlug(slug);
                            configUser.getCircleCIProjects().add(project);

                            sendTextAnswer(absSender, chat, String.format("Проект %s добавлен добавлен в подписки", projectName));
                        } else {
                            sendTextAnswer(absSender, chat, "Неверный путь (slug) проекта");
                        }
                    } else {
                        sendTextAnswer(absSender, chat, "Неверный токен");
                    }
                } catch (Exception e) {
                    sendTextAnswer(absSender, chat, "Ошибка: " + e.getMessage());
                }
            } else if ("del".equalsIgnoreCase(subCommand) && arguments.length >= 2) {
                slug = arguments[1];
                if (slug.toLowerCase().startsWith("gh")) slug = "github" + slug.substring(2);
                if (slug.toLowerCase().startsWith("bb")) slug = "bitbucket" + slug.substring(2);

                final String conditionSlug = slug;
                boolean wasDeleted = configUser.getCircleCIProjects().removeIf(p -> p.getSlug().equals(conditionSlug));
                if (wasDeleted) {
                    sendTextAnswer(absSender, chat, String.format("Проект %s удален из подписок", conditionSlug));
                } else {
                    sendTextAnswer(absSender, chat, "Проект не найден в подписках");
                }
            } else if ("list".equalsIgnoreCase(subCommand)) {
                SendMessage message = new SendMessage();
                message.setChatId(chat.getId().toString());
                if (configUser.getCircleCIProjects().size() > 0) {
                    message.setText(
                            configUser.getCircleCIProjects().stream()
                                    .map(p -> String.format("%s (%s)", p.getName(), p.getSlug()))
                                    .collect(Collectors.joining("\n")));
                } else {
                    message.setText("Нет активных подписок");
                }
                sendAnswer(absSender, message);
            } else if ("clear".equalsIgnoreCase(subCommand)) {
                configUser.setCircleCIProjects(new ArrayList<>());
                sendTextAnswer(absSender, chat, "Лист подписок очищен");
            } else if ("help".equalsIgnoreCase(subCommand)) {
                sendTextAnswer(absSender, chat, getHelpText());
            } else {
                sendTextAnswer(absSender, chat, "Неверные аргументы. Наберите '/circleci help' для справки");
            }
        } else {
            sendTextAnswer(absSender, chat, getHelpText());
        }
    }

    private String getHelpText() {
        return new StringJoiner("\n", "CircleCI Command help:\n", "")
                .add("/circleci add PAT slug - add new CircleCI subscription for 'slug' project with 'PAT' authentication")
                .add("/circleci del slug - delete CircleCI subscription for 'slug' project")
                .add("/circleci list - list active CircleCI subscriptions")
                .add("/circleci clear - clear list of CircleCI subscriptions")
                .add("/circleci help - this message")
                .add("NOTE: 'slug' is case-sensitive (/gh/ORGANIZATION/some-project)")
                .toString();
    }

    private void sendTextAnswer(AbsSender absSender, Chat chat, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        message.setText(text);
        sendAnswer(absSender, message);
    }
}
