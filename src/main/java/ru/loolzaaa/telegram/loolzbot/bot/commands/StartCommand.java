package ru.loolzaaa.telegram.loolzbot.bot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class StartCommand extends CommonCommand {

    public StartCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("/rates - Курс вылют");

        ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder replyKeyboardMarkupBuilder = ReplyKeyboardMarkup.builder();
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkupBuilder
                .keyboardRow(keyboardRow)
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();

        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        message.setReplyMarkup(replyKeyboardMarkup);
        message.setText("Введите трэк номер отправления или выберите функционал из меню");

        sendAnswer(absSender, message);
    }
}
