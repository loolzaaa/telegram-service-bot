package ru.loolzaaa.telegram.servicebot.core.bot.config;

import java.util.List;

public interface BotConfiguration<T> {
    T getUserById(Long id);
    T addUser(Long id);
    List<T> getUsers();
}
