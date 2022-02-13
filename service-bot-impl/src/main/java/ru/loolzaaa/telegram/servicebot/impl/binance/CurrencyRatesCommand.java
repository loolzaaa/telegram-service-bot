package ru.loolzaaa.telegram.servicebot.impl.binance;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;
import ru.loolzaaa.telegram.servicebot.core.command.CommonCommand;

public class CurrencyRatesCommand extends CommonCommand {

    public CurrencyRatesCommand(String commandIdentifier, String description, BotConfiguration configuration) {
        super(commandIdentifier, description, configuration);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        String answer;
        try {
            if ("usd".equals(strings[0])) {
                answer = BinanceCurrencyService.getCurrentAveragePrice("BUSDRUB");
            } else {
                answer = BinanceCurrencyService.getCurrentAveragePrice("ETHUSDT");
            }
        } catch (Exception e) {
            answer = "Ошибка";
        }

        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        message.setText(answer);

        sendAnswer(absSender, message);
    }
}
