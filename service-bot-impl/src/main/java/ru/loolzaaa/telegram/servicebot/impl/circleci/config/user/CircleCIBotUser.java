package ru.loolzaaa.telegram.servicebot.impl.circleci.config.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.loolzaaa.telegram.servicebot.core.bot.config.AbstractUser;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CircleCIBotUser extends AbstractUser {
    private String status = "default"; // default, add-subscription-pat, add-subscription-slug, del-subscription
    private List<CircleCISubscription> subscriptions = new ArrayList<>();
}
