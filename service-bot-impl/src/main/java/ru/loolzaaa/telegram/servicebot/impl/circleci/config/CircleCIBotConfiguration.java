package ru.loolzaaa.telegram.servicebot.impl.circleci.config;

import ru.loolzaaa.telegram.servicebot.core.bot.config.AbstractConfiguration;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.CircleCIBotUser;

import java.util.function.Supplier;

public class CircleCIBotConfiguration extends AbstractConfiguration<CircleCIBotUser> {
    public CircleCIBotConfiguration(Supplier<CircleCIBotUser> userSupplier) {
        super(userSupplier);
    }
}
