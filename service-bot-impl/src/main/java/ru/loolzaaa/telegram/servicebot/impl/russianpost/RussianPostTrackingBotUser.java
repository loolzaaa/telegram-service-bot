package ru.loolzaaa.telegram.servicebot.impl.russianpost;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BaseUser;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RussianPostTrackingBotUser extends BaseUser {
    private List<TrackEntry> trackEntries = new ArrayList<>();
}
