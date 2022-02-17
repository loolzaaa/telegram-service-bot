package ru.loolzaaa.telegram.servicebot.impl.binance.command;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BaseUser;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;
import ru.loolzaaa.telegram.servicebot.core.command.CommonCommand;
import ru.loolzaaa.telegram.servicebot.impl.binance.service.BinanceCurrencyService;

import java.util.List;

public class BinanceCommand extends CommonCommand<BaseUser> {

    public BinanceCommand(String commandIdentifier, String description, BotConfiguration<BaseUser> configuration) {
        super(commandIdentifier, description, configuration);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (arguments.length == 2) {
            String answer;
            try {
                if ("usd".equals(arguments[0])) {
                    answer = BinanceCurrencyService.getCurrentAveragePrice("BUSDRUB");
                } else {
                    answer = BinanceCurrencyService.getCurrentAveragePrice("ETHUSDT");
                }
            } catch (Exception e) {
                answer = "Ошибка";
            }

            Integer callbackMessageId = CommonCommand.getCallbackMessageId();
            if (callbackMessageId != null) {
                EditMessageText editMessageText = EditMessageText.builder()
                        .chatId(chat.getId().toString())
                        .messageId(callbackMessageId)
                        .text(answer)
                        .replyMarkup(mainMenuMarkup())
                        .build();
                sendAnswer(absSender, editMessageText);
            } else {
                sendTextAnswer(absSender, chat, answer, false);
            }
        } else if (arguments.length == 0) {
            mainMenu(absSender, chat);
        } else {
            sendTextAnswer(absSender, chat, "Неверная команда или количество аргументов", false);
        }
    }

    private void mainMenu(AbsSender absSender, Chat chat) {
        InlineKeyboardMarkup replyKeyboard = mainMenuMarkup();

        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        message.setText("Выберите тип конверсии");
        message.setReplyMarkup(replyKeyboard);

        sendAnswer(absSender, message);
    }

    private void sendTextAnswer(AbsSender absSender, Chat chat, String text, boolean parseMarkdown) {
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        message.setText(text);
        if (parseMarkdown) message.setParseMode(ParseMode.MARKDOWNV2);
        sendAnswer(absSender, message);
    }

    private InlineKeyboardMarkup mainMenuMarkup() {
        InlineKeyboardButton USDRUBButton = InlineKeyboardButton.builder().text("USD/RUB").callbackData("/rate usd rub").build();
        InlineKeyboardButton ETHUSDButton = InlineKeyboardButton.builder().text("ETH/USD").callbackData("/rate eth usd").build();

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(USDRUBButton, ETHUSDButton))
                .build();
    }
}
