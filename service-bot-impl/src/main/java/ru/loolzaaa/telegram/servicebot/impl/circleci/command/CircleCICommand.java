package ru.loolzaaa.telegram.servicebot.impl.circleci.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;
import ru.loolzaaa.telegram.servicebot.core.command.CommonCommand;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.CircleCIBotUser;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.CircleCISubscription;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class CircleCICommand extends CommonCommand<CircleCIBotUser> {

    private static final String API_PATH = "https://circleci.com/api/v2";

    public CircleCICommand(String commandIdentifier, String description, BotConfiguration<CircleCIBotUser> configuration) {
        super(commandIdentifier, description, configuration);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (arguments.length >= 1) {
            final String subCommand = arguments[0];
            CircleCIBotUser configUser = configuration.getUserById(user.getId());

            if ("pat".equalsIgnoreCase(subCommand)) {
                patSubCommand(absSender, chat, arguments, configUser);
            } else if ("slug".equalsIgnoreCase(subCommand)) {
                slugSubCommand(absSender, chat, arguments, configUser);
            } else if ("add".equalsIgnoreCase(subCommand)) {
                addSubCommand(absSender, chat, arguments, configUser);
            } else if ("del".equalsIgnoreCase(subCommand)) {
                delSubCommand(absSender, chat, arguments, configUser);
            } else if ("list".equalsIgnoreCase(subCommand) && "default".equals(configUser.getStatus())) {
                SendMessage message = new SendMessage();
                message.setChatId(chat.getId().toString());
                if (configUser.getSubscriptions().size() > 0) {
                    message.setText(
                            configUser.getSubscriptions().stream()
                                    .map(s -> String.format("%s (%s)", s.getName(), s.getSlug()))
                                    .collect(Collectors.joining("\n")));
                } else {
                    message.setText("Нет активных подписок");
                }
                sendAnswer(absSender, message);
            } else if ("clear".equalsIgnoreCase(subCommand) && "default".equals(configUser.getStatus())) {
                configUser.setSubscriptions(new ArrayList<>());
                sendTextAnswer(absSender, chat, "Лист подписок очищен");
            } else if ("help".equalsIgnoreCase(subCommand) && "default".equals(configUser.getStatus())) {
                sendTextAnswer(absSender, chat, getHelpText());
            } else if ("break".equalsIgnoreCase(subCommand) && "breaking".equals(configUser.getStatus())) {
                configUser.setStatus("default");
                sendTextAnswer(absSender, chat, "Прервано");
            } else {
                sendTextAnswer(absSender, chat, "Неверная команда или аргументы.\nНаберите '/circleci help' для справки.");
            }
        } else {
            InlineKeyboardButton addButton = InlineKeyboardButton.builder().text("Добавить").callbackData("/circleci add").build();
            InlineKeyboardButton delButton = InlineKeyboardButton.builder().text("Удалить").callbackData("/circleci del").build();
            InlineKeyboardButton listButton = InlineKeyboardButton.builder().text("Показать все").callbackData("/circleci list").build();
            InlineKeyboardButton helpButton = InlineKeyboardButton.builder().text("Справка").callbackData("/circleci help").build();

            InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                    .keyboardRow(List.of(addButton, delButton))
                    .keyboardRow(List.of(listButton, helpButton))
                    .build();

            SendMessage message = new SendMessage();
            message.setChatId(chat.getId().toString());
            message.setText("Выберите действие для подписки:");
            message.setReplyMarkup(keyboard);

            sendAnswer(absSender, message);
        }
    }

    private void addSubCommand(AbsSender absSender, Chat chat, String[] arguments, CircleCIBotUser configUser) {
        if (arguments.length >= 3 && "default".equals(configUser.getStatus())) {
            String pat = arguments[1];
            String slug = arguments[2];
            if (slug.toLowerCase().startsWith("gh")) slug = "github" + slug.substring(2);
            if (slug.toLowerCase().startsWith("bb")) slug = "bitbucket" + slug.substring(2);
            List<String> slugs = configUser.getSubscriptions().stream()
                    .map(subscription -> subscription.getSlug().toLowerCase())
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
                        CircleCISubscription subscription = new CircleCISubscription();
                        subscription.setName(projectName);
                        subscription.setSlug(slug);
                        configUser.getSubscriptions().add(subscription);

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
        } else if ("default".equals(configUser.getStatus())) {
            sendTextAnswer(absSender, chat, "Введите ваш персональный токен");
            configUser.setStatus("add-subscription-pat");
        }
    }

    private void delSubCommand(AbsSender absSender, Chat chat, String[] arguments, CircleCIBotUser configUser) {
        if (arguments.length >= 2 && "del-subscription".equals(configUser.getStatus())) {
            String slug = arguments[1];
            if (slug.toLowerCase().startsWith("gh")) slug = "github" + slug.substring(2);
            if (slug.toLowerCase().startsWith("bb")) slug = "bitbucket" + slug.substring(2);

            final String conditionSlug = slug;
            boolean wasDeleted = configUser.getSubscriptions().removeIf(s -> s.getSlug().equals(conditionSlug));
            if (wasDeleted) {
                sendTextAnswer(absSender, chat, String.format("Проект %s удален из подписок", conditionSlug));
            } else {
                sendTextAnswer(absSender, chat, "Проект не найден в подписках");
            }
            configUser.setStatus("default");
        } else if ("default".equals(configUser.getStatus())) {
            sendTextAnswer(absSender, chat, "Введите 'slug' проекта");
            configUser.setStatus("del-subscription");
        }
    }

    private void patSubCommand(AbsSender absSender, Chat chat, String[] arguments, CircleCIBotUser configUser) {
        if (arguments.length >= 2 && "add-subscription-pat".equals(configUser.getStatus())) {
            final String pat = arguments[1];
            HttpRequest request = HttpRequest.newBuilder(URI.create(API_PATH + "/me"))
                    .header("Circle-Token", pat)
                    .GET()
                    .build();
            try {
                int code = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
                if (code == 200) {
                    CircleCISubscription subscription = new CircleCISubscription();
                    subscription.setPat(pat);
                    configUser.getSubscriptions().add(subscription);

                    sendTextAnswer(absSender, chat, "Введите 'slug' проекта");

                    configUser.setStatus("add-subscription-slug");
                } else {
                    sendTextAnswer(absSender, chat, "Неверный токен");
                }
            } catch (Exception e) {
                configUser.setStatus("default");
                sendTextAnswer(absSender, chat, "Ошибка: " + e.getMessage());
            }
        }
    }

    private void slugSubCommand(AbsSender absSender, Chat chat, String[] arguments, CircleCIBotUser configUser) {
        if (arguments.length >= 2 && "add-subscription-slug".equals(configUser.getStatus())) {
            String slug = arguments[1];
            if (slug.toLowerCase().startsWith("gh")) slug = "github" + slug.substring(2);
            if (slug.toLowerCase().startsWith("bb")) slug = "bitbucket" + slug.substring(2);
            List<String> slugs = configUser.getSubscriptions().stream()
                    .filter(subscription -> subscription.getSlug() != null)
                    .map(subscription -> subscription.getSlug().toLowerCase())
                    .collect(Collectors.toList());
            if (slugs.contains(slug.toLowerCase())) {
                configUser.setStatus("default");
                configUser.getSubscriptions().removeIf(subscription -> subscription.getSlug() == null);
                sendTextAnswer(absSender, chat, "Данный проект уже содержится в листе подписок");
                return;
            }

            Optional<CircleCISubscription> subscriptionOptional = configUser.getSubscriptions().stream()
                    .filter(s -> s.getSlug() == null)
                    .findFirst();
            if (subscriptionOptional.isPresent()) {
                CircleCISubscription subscription = subscriptionOptional.get();
                HttpRequest request = HttpRequest.newBuilder(URI.create(API_PATH + "/project/" + slug))
                        .header("Circle-Token", subscription.getPat())
                        .GET()
                        .build();
                try {
                    HttpResponse<String> projectResponse = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                    if (projectResponse.statusCode() == 200) {
                        JsonNode projectNode = new ObjectMapper().readTree(projectResponse.body());
                        final String projectName = projectNode.get("name").asText();
                        subscription.setName(projectName);
                        subscription.setSlug(slug);

                        sendTextAnswer(absSender, chat, String.format("Проект %s добавлен добавлен в подписки", projectName));

                        configUser.setStatus("default");
                    } else {
                        sendTextAnswer(absSender, chat, "Неверный путь (slug) проекта");
                    }
                } catch (Exception e) {
                    configUser.setStatus("default");
                    sendTextAnswer(absSender, chat, "Ошибка: " + e.getMessage());
                }
            } else {
                configUser.setStatus("default");
                sendTextAnswer(absSender, chat, "Что-то пошло не так.\nНе могу найти создаваемую подписку.");
            }
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
