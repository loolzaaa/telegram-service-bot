package ru.loolzaaa.telegram.servicebot.impl.circleci.config;

import ru.loolzaaa.telegram.servicebot.core.bot.config.AbstractConfiguration;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.BotUser;

import java.util.function.Supplier;

public class CircleCIBotConfiguration extends AbstractConfiguration<BotUser> {
    public CircleCIBotConfiguration(Supplier<BotUser> userSupplier) {
        super(userSupplier);
    }
}
