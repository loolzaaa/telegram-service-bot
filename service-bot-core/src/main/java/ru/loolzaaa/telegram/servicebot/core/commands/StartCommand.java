package ru.loolzaaa.telegram.servicebot.core.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.loolzaaa.telegram.servicebot.core.bot.pojo.Configuration;

public class StartCommand extends CommonCommand {

    public StartCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    public StartCommand(String commandIdentifier, String description, Configuration configuration) {
        super(commandIdentifier, description, configuration);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        KeyboardRow ratesRow = new KeyboardRow();
        ratesRow.add("/rates - Курс валют");
        KeyboardRow trackHistoryRow = new KeyboardRow();
        trackHistoryRow.add("/trackhistory - История отслеживаний");

        ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder replyKeyboardMarkupBuilder = ReplyKeyboardMarkup.builder();
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkupBuilder
                .keyboardRow(ratesRow)
                .keyboardRow(trackHistoryRow)
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();

        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        message.setReplyMarkup(replyKeyboardMarkup);
        message.setText("Введите трек номер отправления или выберите функционал из меню");

        sendAnswer(absSender, message);
    }
}
