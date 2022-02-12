package ru.loolzaaa.telegram.servicebot.core.bot.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Configuration {

    private List<User> users;

    public User getUserById(Long id) {
        for (User u : users) {
            if (u.getId().equals(id)) return u;
        }
        return null;
    }
}
