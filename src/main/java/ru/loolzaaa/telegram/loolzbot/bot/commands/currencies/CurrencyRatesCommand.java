package ru.loolzaaa.telegram.loolzbot.bot.commands.currencies;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.loolzaaa.telegram.loolzbot.bot.commands.CommonCommand;
import ru.loolzaaa.telegram.loolzbot.service.BinanceCurrencyService;

public class CurrencyRatesCommand extends CommonCommand {

    public CurrencyRatesCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
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
