package ru.loolzaaa.telegram.servicebot.impl.circleci.config.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Subscription {
    private String name;
    private String slug;
    private String pat;
}
