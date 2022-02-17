package ru.loolzaaa.telegram.servicebot.impl.circleci.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BaseConfiguration;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.BotUser;

import java.util.List;

public class CircleCIBotConfiguration extends BaseConfiguration<BotUser> {
    @JsonCreator
    public CircleCIBotConfiguration(@JsonProperty("users") List<BotUser> users) {
        super(users, BotUser::new);
    }
}
