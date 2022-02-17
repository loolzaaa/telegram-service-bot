package ru.loolzaaa.telegram.servicebot.impl.binance;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.loolzaaa.telegram.servicebot.core.bot.ServiceWebhookBot;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BaseUser;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;
import ru.loolzaaa.telegram.servicebot.impl.binance.command.BinanceCommand;
import ru.loolzaaa.telegram.servicebot.impl.binance.command.HelpCommand;
import ru.loolzaaa.telegram.servicebot.impl.circleci.helper.BotHelper;

public class BinanceWebhookBot extends ServiceWebhookBot<BaseUser> {

    public BinanceWebhookBot(BotConfiguration<BaseUser> configuration, String botPath, String nameVariable, String tokenVariable) {
        super(configuration, botPath, nameVariable, tokenVariable);
        register(new BinanceCommand("rate", "Binance bot main functional", configuration));
        register(new HelpCommand("help", "Help for Binance bot", configuration));
    }

    public BinanceWebhookBot(BotConfiguration<BaseUser> configuration, String botPath) {
        this(configuration, botPath, "binance_bot_name",  "binance_bot_token");
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().isCommand() && update.getMessage().getText().startsWith("/start")) {
                BotHelper.changeMessageToCommand(update, "/rate");
            }
        }
        return super.onWebhookUpdateReceived(update);
    }
}
