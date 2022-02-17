package ru.loolzaaa.telegram.servicebot.impl.russianpost;

import ru.loolzaaa.telegram.servicebot.core.bot.config.BaseConfiguration;

import java.util.List;
import java.util.function.Supplier;

public class RussianPostTrackingBotConfiguration extends BaseConfiguration<RussianPostTrackingBotUser> {
    public RussianPostTrackingBotConfiguration(List<RussianPostTrackingBotUser> users, Supplier<RussianPostTrackingBotUser> userSupplier) {
        super(users, userSupplier);
    }
}
