package ru.loolzaaa.telegram.servicebot.core.bot.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class BaseUser {
    private Long id;
    private String firstName;
    private String username;
    private Long chatId;
    private LocalDateTime lastActivity;

    protected BaseUser(Long id) {
        this.id = id;
    }
}
