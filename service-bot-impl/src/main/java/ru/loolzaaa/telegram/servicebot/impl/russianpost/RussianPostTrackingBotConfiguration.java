package ru.loolzaaa.telegram.servicebot.impl.russianpost;

import ru.loolzaaa.telegram.servicebot.core.bot.config.AbstractConfiguration;

import java.util.function.Supplier;

public class RussianPostTrackingBotConfiguration extends AbstractConfiguration<RussianPostTrackingBotUser> {
    public RussianPostTrackingBotConfiguration(Supplier<RussianPostTrackingBotUser> userSupplier) {
        super(userSupplier);
    }
}
