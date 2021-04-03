package ru.loolzaaa.telegram.loolzbot.bot.pojo;

import java.util.List;

public class Configuration {

    private List<User> users;

    public User getUserById(Long id) {
        for (User u : users) {
            if (u.getId().equals(id)) return u;
        }
        return null;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
