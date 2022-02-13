package ru.loolzaaa.telegram.servicebot.impl.circleci;

import ru.loolzaaa.telegram.servicebot.core.bot.config.AbstractConfiguration;

import java.util.function.Supplier;

public class CircleCIBotConfiguration extends AbstractConfiguration<CircleCIBotUser> {
    public CircleCIBotConfiguration(Supplier<CircleCIBotUser> userSupplier) {
        super(userSupplier);
    }
}
