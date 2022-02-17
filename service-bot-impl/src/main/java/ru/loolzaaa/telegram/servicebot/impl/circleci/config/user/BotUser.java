package ru.loolzaaa.telegram.servicebot.impl.circleci.config.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BaseUser;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BotUser extends BaseUser {
    private String localeKey = "en";
    private BotUserStatus status = BotUserStatus.DEFAULT;
    private List<Subscription> subscriptions = new ArrayList<>();

    public void clearUnfinishedSubscriptions() {
        subscriptions.removeIf(s -> s.getPat() == null || s.getSlug() == null);
    }
}
