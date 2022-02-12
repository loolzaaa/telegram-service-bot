package ru.loolzaaa.telegram.servicebot.core.bot.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.loolzaaa.telegram.servicebot.core.circleci.pojo.Project;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class User {
    private Long id;
    private String firstName;
    private String username;
    private Long chatId;
    private LocalDateTime lastActivity;
    private List<TrackEntry> trackHistory;
    private List<Project> circleCIProjects;
}
