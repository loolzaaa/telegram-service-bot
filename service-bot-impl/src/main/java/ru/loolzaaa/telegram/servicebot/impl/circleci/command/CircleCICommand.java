package ru.loolzaaa.telegram.servicebot.impl.circleci.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;
import ru.loolzaaa.telegram.servicebot.core.command.CommonCommand;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.BotUser;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.BotUserStatus;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.Subscription;
import ru.loolzaaa.telegram.servicebot.impl.circleci.helper.I18n;

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
            if (configUser == null) {
                removeCallbackMessage(absSender, chat);
                sendTextAnswer(absSender, chat, "Something strange... Type /start", false);
                return;
            }

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
                    listSubCommand(absSender, chat, arguments, configUser);
                } else if ("help".equalsIgnoreCase(subCommand) && (configUser.getStatus() == BotUserStatus.DEFAULT)) {
                    helpSubCommand(absSender, chat, arguments, configUser);
                } else if ("settings".equalsIgnoreCase(subCommand) && (configUser.getStatus() == BotUserStatus.DEFAULT)) {
                    settingsSubCommand(absSender, chat, arguments, configUser);
                } else if ("clear".equalsIgnoreCase(subCommand) && (configUser.getStatus() == BotUserStatus.DEFAULT)) {
                    configUser.getSubscriptions().clear();
                    sendTextAnswer(absSender, chat, I18n.get("clearCommandSuccess"), false);
                    mainMenu(absSender, chat);
                } else if ("break".equalsIgnoreCase(subCommand) && (configUser.getStatus() == BotUserStatus.BREAKING)) {
                    configUser.setStatus(BotUserStatus.DEFAULT);
                    sendTextAnswer(absSender, chat, I18n.get("breakCommandSuccess"), false);
                    mainMenu(absSender, chat);
                } else {
                    sendTextAnswer(absSender, chat, I18n.get("invalidCommandOrArgs"), false);
                }
            } catch (Exception e) {
                configUser.setStatus(BotUserStatus.DEFAULT);
                configUser.clearUnfinishedSubscriptions();
                sendTextAnswer(absSender, chat, I18n.get("exceptionError"), false);
                mainMenu(absSender, chat);
            }
        } else {
            mainMenu(absSender, chat);
        }
    }

    private void mainMenu(AbsSender absSender, Chat chat) {
        InlineKeyboardMarkup keyboard = mainMenuInlineKeyboardMarkup();

        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        message.setText(I18n.get("mainMenuText"));
        message.setReplyMarkup(keyboard);

        sendAnswer(absSender, message);
    }

    private void addSubCommand(AbsSender absSender, Chat chat, String[] arguments, BotUser configUser) throws Exception{
        if (arguments.length == 3 && (configUser.getStatus() == BotUserStatus.DEFAULT)) {
            boolean patOk = patSubCommand(absSender, chat, arguments, configUser);
            if (patOk) {
                slugSubCommand(absSender, chat, arguments, configUser);
            }
        } else if (arguments.length == 1 && (configUser.getStatus() == BotUserStatus.DEFAULT)) {
            removeCallbackMessage(absSender, chat);
            sendTextAnswer(absSender, chat, I18n.get("addCommandTokenInput"), false);
            configUser.setStatus(BotUserStatus.ADD_SUBSCRIPTION_PAT);
        } else {
            sendTextAnswer(absSender, chat, I18n.get("invalidCommandOrArgs"), false);
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
                sendTextAnswer(absSender, chat, I18n.get("delCommandSuccess", conditionSlug), false);
            } else {
                sendTextAnswer(absSender, chat, I18n.get("delCommandNotFound"), false);
            }
            configUser.setStatus(BotUserStatus.DEFAULT);
            mainMenu(absSender, chat);
        } else if (arguments.length == 1 && configUser.getStatus() == BotUserStatus.DEFAULT) {
            removeCallbackMessage(absSender, chat);
            sendTextAnswer(absSender, chat, I18n.get("delCommandSlugInput"), false);
            configUser.setStatus(BotUserStatus.DEL_SUBSCRIPTION);
        } else {
            sendTextAnswer(absSender, chat, I18n.get("invalidCommandOrArgs"), false);
        }
    }

    private boolean patSubCommand(AbsSender absSender, Chat chat, String[] arguments, BotUser configUser) throws Exception {
        boolean fromAddSubCommand = arguments.length == 3 && (configUser.getStatus() == BotUserStatus.DEFAULT);
        boolean fromMainCommand = arguments.length == 2 && (configUser.getStatus() == BotUserStatus.ADD_SUBSCRIPTION_PAT);
        if (fromAddSubCommand || fromMainCommand) {
            removeCallbackMessage(absSender, chat); //Remove potential token from chat
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
                    sendTextAnswer(absSender, chat, I18n.get("patCommandSlugInput"), false);
                }
                return true;
            } else {
                sendTextAnswer(absSender, chat, I18n.get("patCommandTokenIncorrect"), false);
                return false;
            }
        } else if (configUser.getStatus() == BotUserStatus.ADD_SUBSCRIPTION_PAT) {
            removeCallbackMessage(absSender, chat); //Remove potential token from chat
            sendTextAnswer(absSender, chat, I18n.get("patCommandTokenIncorrect"), false);
            return false;
        } else {
            sendTextAnswer(absSender, chat, I18n.get("invalidCommandOrArgs"), false);
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
                sendTextAnswer(absSender, chat, I18n.get("slugCommandAlreadyExist"), false);
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

                sendTextAnswer(absSender, chat, I18n.get("slugCommandSuccess", projectName), false);
                mainMenu(absSender, chat);
                return true;
            } else {
                sendTextAnswer(absSender, chat, I18n.get("slugCommandSlugIncorrect"), false);
                return false;
            }
        } else if (configUser.getStatus() == BotUserStatus.ADD_SUBSCRIPTION_SLUG) {
            sendTextAnswer(absSender, chat, I18n.get("slugCommandSlugIncorrect"), false);
            return false;
        } else {
            sendTextAnswer(absSender, chat, I18n.get("invalidCommandOrArgs"), false);
            return false;
        }
    }

    private void listSubCommand(AbsSender absSender, Chat chat, String[] arguments, BotUser configUser) {
        String listText = I18n.get("listCommandEmpty");
        if (configUser.getSubscriptions().size() > 0) {
            listText = configUser.getSubscriptions().stream()
                    .map(s -> I18n.get("listCommandFormat", s.getName(), s.getSlug()))
                    .collect(Collectors.joining("\n"));
        }
        removeCallbackMessage(absSender, chat);
        sendTextAnswer(absSender, chat, listText, false);
        mainMenu(absSender, chat);
    }

    private void helpSubCommand(AbsSender absSender, Chat chat, String[] arguments, BotUser configUser) {
        String helpText = new StringJoiner("\n", "*CircleCI Command help:*\n\n", "")
                .add("`/circleci` \\- main menu for CircleCI Results Bot\n")
                .add("`/circleci add \\<pat\\> \\<slug\\>` \\- add new CircleCI subscription for 'slug' project with 'PAT' authentication\n")
                .add("`/circleci del \\<slug\\>` \\- delete CircleCI subscription for 'slug' project\n")
                .add("`/circleci list` \\- list active CircleCI subscriptions\n")
                .add("`/circleci clear` \\- clear list of CircleCI subscriptions\\.\n*__WARNING\\! No confirmation is needed\\!__*\n")
                .add("`/circleci help` \\- this message\n")
                .add("*_NOTE: 'slug' is case\\-sensitive\n\\(/gh/ORGANIZATION/some\\-project\\)_*")
                .toString();
        removeCallbackMessage(absSender, chat);
        sendTextAnswer(absSender, chat, helpText, true);
        mainMenu(absSender, chat);
    }

    private void settingsSubCommand(AbsSender absSender, Chat chat, String[] arguments, BotUser configUser) {
        if (arguments.length == 3 && configUser.getStatus() == BotUserStatus.DEFAULT) {
            Integer callbackMessageId = CommonCommand.getCallbackMessageId();
            if (callbackMessageId != null) {
                if ("lang".equalsIgnoreCase(arguments[1])) {
                    String localeKey = arguments[2];
                    configUser.setLocaleKey(localeKey);
                    I18n.setCurrentBundle(localeKey);
                    removeCallbackMessage(absSender, chat);
                    mainMenu(absSender, chat);
                }
            }
        } else if (arguments.length == 2 && configUser.getStatus() == BotUserStatus.DEFAULT) {
            Integer callbackMessageId = CommonCommand.getCallbackMessageId();
            if (callbackMessageId != null) {
                EditMessageText editMessageText = EditMessageText.builder()
                        .chatId(chat.getId().toString())
                        .messageId(callbackMessageId)
                        .text(I18n.get("mainMenuText"))
                        .replyMarkup(mainMenuInlineKeyboardMarkup())
                        .build();
                sendAnswer(absSender, editMessageText);
            }
        } else if (arguments.length == 1 && configUser.getStatus() == BotUserStatus.DEFAULT) {
            Integer callbackMessageId = CommonCommand.getCallbackMessageId();
            if (callbackMessageId != null) {
                InlineKeyboardButton enButton = InlineKeyboardButton.builder().text("English").callbackData("/circleci settings lang en").build();
                InlineKeyboardButton ruButton = InlineKeyboardButton.builder().text("Русский").callbackData("/circleci settings lang ru").build();
                InlineKeyboardButton backButton = InlineKeyboardButton.builder().text(I18n.get("settingsMenuBackBtn")).callbackData("/circleci settings back").build();

                InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                        .keyboardRow(List.of(enButton, ruButton))
                        .keyboardRow(List.of(backButton))
                        .build();

                EditMessageText editMessageText = EditMessageText.builder()
                        .chatId(chat.getId().toString())
                        .messageId(callbackMessageId)
                        .text(I18n.get("settingsMenuText"))
                        .replyMarkup(keyboard)
                        .build();
                sendAnswer(absSender, editMessageText);
            }
        }
    }

    private void sendTextAnswer(AbsSender absSender, Chat chat, String text, boolean parseMarkdown) {
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        message.setText(text);
        if (parseMarkdown) message.setParseMode(ParseMode.MARKDOWNV2);
        sendAnswer(absSender, message);
    }

    private void removeCallbackMessage(AbsSender absSender, Chat chat) {
        Integer callbackMessageId = CommonCommand.getCallbackMessageId();
        if (callbackMessageId != null) {
            DeleteMessage deleteMessage = DeleteMessage.builder()
                    .chatId(chat.getId().toString())
                    .messageId(callbackMessageId)
                    .build();
            sendAnswer(absSender, deleteMessage);
            CommonCommand.setCallbackMessageId(null);
        }
    }

    private InlineKeyboardMarkup mainMenuInlineKeyboardMarkup() {
        InlineKeyboardButton addButton = InlineKeyboardButton.builder().text(I18n.get("mainMenuAddBtn")).callbackData("/circleci add").build();
        InlineKeyboardButton delButton = InlineKeyboardButton.builder().text(I18n.get("mainMenuDelBtn")).callbackData("/circleci del").build();
        InlineKeyboardButton listButton = InlineKeyboardButton.builder().text(I18n.get("mainMenuListBtn")).callbackData("/circleci list").build();
        InlineKeyboardButton helpButton = InlineKeyboardButton.builder().text(I18n.get("mainMenuHelpBtn")).callbackData("/circleci help").build();
        InlineKeyboardButton settingsButton = InlineKeyboardButton.builder().text(I18n.get("mainMenuSettingsBtn")).callbackData("/circleci settings").build();

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(addButton, delButton))
                .keyboardRow(List.of(listButton, helpButton))
                .keyboardRow(List.of(settingsButton))
                .build();
    }
}
