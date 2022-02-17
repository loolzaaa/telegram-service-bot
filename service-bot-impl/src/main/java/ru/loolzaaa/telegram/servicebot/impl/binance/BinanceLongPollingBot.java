package ru.loolzaaa.telegram.servicebot.impl.binance;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.loolzaaa.telegram.servicebot.core.bot.ServiceLongPollingBot;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BaseUser;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;
import ru.loolzaaa.telegram.servicebot.impl.binance.command.BinanceCommand;
import ru.loolzaaa.telegram.servicebot.impl.binance.command.HelpCommand;
import ru.loolzaaa.telegram.servicebot.impl.circleci.helper.BotHelper;

import java.util.List;

public class BinanceLongPollingBot extends ServiceLongPollingBot<BaseUser> {

    public BinanceLongPollingBot(BotConfiguration<BaseUser> configuration, String nameVariable, String tokenVariable) {
        super(configuration, nameVariable, tokenVariable);
        register(new BinanceCommand("rate", "Binance bot main functional", configuration));
        register(new HelpCommand("help", "Help for Binance bot", configuration));
    }

    public BinanceLongPollingBot(BotConfiguration<BaseUser> configuration) {
        this(configuration, "binance_bot_name",  "binance_bot_token");
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        for (Update update : updates) {
            if (update.hasMessage()) {
                if (update.getMessage().isCommand() && update.getMessage().getText().startsWith("/start")) {
                    BotHelper.changeMessageToCommand(update, "/rate");
                }
            }
        }
        super.onUpdatesReceived(updates);
    }
}
