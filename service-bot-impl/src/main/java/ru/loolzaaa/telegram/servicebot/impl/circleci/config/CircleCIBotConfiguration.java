package ru.loolzaaa.telegram.servicebot.impl.circleci.config;

import ru.loolzaaa.telegram.servicebot.core.bot.config.AbstractConfiguration;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.BotUser;

import java.util.List;
import java.util.function.Supplier;

public class CircleCIBotConfiguration extends AbstractConfiguration<BotUser> {
    public CircleCIBotConfiguration(List<BotUser> users, Supplier<BotUser> userSupplier) {
        super(users, userSupplier);
    }
}
