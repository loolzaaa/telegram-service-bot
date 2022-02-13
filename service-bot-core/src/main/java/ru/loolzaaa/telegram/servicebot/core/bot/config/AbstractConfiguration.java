package ru.loolzaaa.telegram.servicebot.core.bot.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractConfiguration<T extends AbstractUser> implements BotConfiguration<T> {

    private final List<T> users = new ArrayList<>();

    private Supplier<T> userSupplier;

    protected AbstractConfiguration(Supplier<T> userSupplier) {
        this.userSupplier = userSupplier;
    }

    @Override
    public T getUserById(Long id) {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public T addUser(Long id) {
        T user = getUserById(id);
        if (user != null) {
            return user;
        }
        user = userSupplier.get();
        user.setId(id);
        users.add(user);
        return user;
    }

    @Override
    public List<T> getUsers() {
        return users;
    }
}
