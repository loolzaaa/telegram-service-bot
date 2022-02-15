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
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.BotUser;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.Subscription;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.BotUserStatus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class CircleCICommand extends CommonCommand<BotUser> {

    private static final String API_PATH = "https://circleci.com/api/v2";

    public CircleCICommand(String commandIdentifier, String description, BotConfiguration<BotUser> configuration) {
        super(commandIdentifier, description, configuration);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (arguments.length >= 1) {
            final String subCommand = arguments[0];
            BotUser configUser = configuration.getUserById(user.getId());

            try {
                if ("pat".equalsIgnoreCase(subCommand) && (configUser.getStatus() == BotUserStatus.ADD_SUBSCRIPTION_PAT)) {
                    patSubCommand(absSender, chat, arguments, configUser);
                } else if ("slug".equalsIgnoreCase(subCommand) && (configUser.getStatus() == BotUserStatus.ADD_SUBSCRIPTION_SLUG)) {
                    slugSubCommand(absSender, chat, arguments, configUser);
                } else if ("add".equalsIgnoreCase(subCommand)) {
                    addSubCommand(absSender, chat, arguments, configUser);
                } else if ("del".equalsIgnoreCase(subCommand)) {
                    delSubCommand(absSender, chat, arguments, configUser);
                } else if ("list".equalsIgnoreCase(subCommand) && (configUser.getStatus() == BotUserStatus.DEFAULT)) {
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
                } else if ("clear".equalsIgnoreCase(subCommand) && (configUser.getStatus() == BotUserStatus.DEFAULT)) {
                    configUser.getSubscriptions().clear();
                    sendTextAnswer(absSender, chat, "Лист подписок очищен");
                } else if ("help".equalsIgnoreCase(subCommand) && (configUser.getStatus() == BotUserStatus.DEFAULT)) {
                    sendTextAnswer(absSender, chat, getHelpText());
                } else if ("break".equalsIgnoreCase(subCommand) && (configUser.getStatus() == BotUserStatus.BREAKING)) {
                    configUser.setStatus(BotUserStatus.DEFAULT);
                    sendTextAnswer(absSender, chat, "Прервано");
                } else {
                    sendTextAnswer(absSender, chat, "Неверная команда или аргументы.\nНаберите '/circleci help' для справки.");
                }
            } catch (Exception e) {
                configUser.setStatus(BotUserStatus.DEFAULT);
                configUser.clearUnfinishedSubscriptions();
                sendTextAnswer(absSender, chat, "Ошибка. Попробуйте позже");
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

    private void addSubCommand(AbsSender absSender, Chat chat, String[] arguments, BotUser configUser) throws Exception{
        if (arguments.length == 3 && (configUser.getStatus() == BotUserStatus.DEFAULT)) {
            boolean patOk = patSubCommand(absSender, chat, arguments, configUser);
            if (patOk) {
                slugSubCommand(absSender, chat, arguments, configUser);
            }
        } else if (arguments.length == 1 && (configUser.getStatus() == BotUserStatus.DEFAULT)) {
            sendTextAnswer(absSender, chat, "Введите ваш персональный токен");
            configUser.setStatus(BotUserStatus.ADD_SUBSCRIPTION_PAT);
        } else {
            sendTextAnswer(absSender, chat, "Неверная команда или аргументы\nНаберите '/circleci help' для справки");
        }
    }

    private void delSubCommand(AbsSender absSender, Chat chat, String[] arguments, BotUser configUser) {
        boolean validStatus = configUser.getStatus() == BotUserStatus.DEFAULT || configUser.getStatus() == BotUserStatus.DEL_SUBSCRIPTION;
        if (arguments.length == 2 && validStatus) {
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
            configUser.setStatus(BotUserStatus.DEFAULT);
        } else if (arguments.length == 1 && configUser.getStatus() == BotUserStatus.DEFAULT) {
            sendTextAnswer(absSender, chat, "Введите 'slug' проекта");
            configUser.setStatus(BotUserStatus.DEL_SUBSCRIPTION);
        } else {
            sendTextAnswer(absSender, chat, "Неверная команда или аргументы\nНаберите '/circleci help' для справки");
        }
    }

    private boolean patSubCommand(AbsSender absSender, Chat chat, String[] arguments, BotUser configUser) throws Exception {
        boolean fromAddSubCommand = arguments.length == 3 && (configUser.getStatus() == BotUserStatus.DEFAULT);
        boolean fromMainCommand = arguments.length == 2 && (configUser.getStatus() == BotUserStatus.ADD_SUBSCRIPTION_PAT);
        if (fromAddSubCommand || fromMainCommand) {
            final String pat = arguments[1];
            HttpRequest request = HttpRequest.newBuilder(URI.create(API_PATH + "/me"))
                    .header("Circle-Token", pat)
                    .GET()
                    .build();
            int code = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
            if (code == 200) {
                configUser.setStatus(BotUserStatus.ADD_SUBSCRIPTION_SLUG);

                Subscription subscription = new Subscription();
                subscription.setPat(pat);
                configUser.getSubscriptions().add(subscription); //Must be only one place where add subscription

                if (fromMainCommand) {
                    sendTextAnswer(absSender, chat, "Введите 'slug' проекта");
                }
                return true;
            } else {
                sendTextAnswer(absSender, chat, "Неверный токен\nПопробуйте еще раз");
                return false;
            }
        } else if (configUser.getStatus() == BotUserStatus.ADD_SUBSCRIPTION_PAT) {
            sendTextAnswer(absSender, chat, "Неверный токен\nПопробуйте еще раз");
            return false;
        } else {
            sendTextAnswer(absSender, chat, "Неверная команда или аргументы\nНаберите '/circleci help' для справки");
            return false;
        }
    }

    private boolean slugSubCommand(AbsSender absSender, Chat chat, String[] arguments, BotUser configUser) throws Exception {
        boolean fromAddSubCommand = arguments.length == 3 && (configUser.getStatus() == BotUserStatus.ADD_SUBSCRIPTION_SLUG);
        boolean fromMainCommand = arguments.length == 2 && (configUser.getStatus() == BotUserStatus.ADD_SUBSCRIPTION_SLUG);
        if (fromAddSubCommand || fromMainCommand) {
            String slug = fromMainCommand ? arguments[1] : arguments[2];
            if (slug.toLowerCase().startsWith("gh")) slug = "github" + slug.substring(2);
            if (slug.toLowerCase().startsWith("bb")) slug = "bitbucket" + slug.substring(2);
            List<String> slugs = configUser.getSubscriptions().stream()
                    .filter(subscription -> subscription.getSlug() != null)
                    .map(subscription -> subscription.getSlug().toLowerCase())
                    .collect(Collectors.toList());
            if (slugs.contains(slug.toLowerCase())) {
                configUser.setStatus(BotUserStatus.DEFAULT);
                configUser.clearUnfinishedSubscriptions();
                sendTextAnswer(absSender, chat, "Данный проект уже содержится в листе подписок");
                return false;
            }

            Subscription subscription = configUser.getSubscriptions().stream()
                    .filter(s -> s.getSlug() == null) //Must be only one subscription with slug = null
                    .findFirst()
                    .orElseThrow(() -> new NullPointerException("Can't find unfinished subscription!"));
            HttpRequest request = HttpRequest.newBuilder(URI.create(API_PATH + "/project/" + slug))
                    .header("Circle-Token", subscription.getPat())
                    .GET()
                    .build();
            HttpResponse<String> projectResponse = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (projectResponse.statusCode() == 200) {
                JsonNode projectNode = new ObjectMapper().readTree(projectResponse.body());
                final String projectName = projectNode.get("name").asText();

                configUser.setStatus(BotUserStatus.DEFAULT);

                subscription.setName(projectName); //Must be only one place where set subscription name
                subscription.setSlug(slug); //Must be only one place where set subscription slug

                sendTextAnswer(absSender, chat, String.format("Проект %s добавлен добавлен в подписки", projectName));
                return true;
            } else {
                sendTextAnswer(absSender, chat, "Неверный путь (slug) проекта");
                return false;
            }
        } else if (configUser.getStatus() == BotUserStatus.ADD_SUBSCRIPTION_SLUG) {
            sendTextAnswer(absSender, chat, "Неверный путь (slug) проекта");
            return false;
        } else {
            sendTextAnswer(absSender, chat, "Неверная команда или аргументы\nНаберите '/circleci help' для справки");
            return false;
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
