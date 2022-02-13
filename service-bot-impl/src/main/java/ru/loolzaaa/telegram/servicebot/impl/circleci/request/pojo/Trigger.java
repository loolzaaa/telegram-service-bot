package ru.loolzaaa.telegram.servicebot.impl.circleci.request.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Trigger {
    private String type;
}
