package ru.loolzaaa.telegram.servicebot.core.circleci.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pipeline {
    private String id;
    private int number;
    @JsonProperty("created_at")
    private String createdAt;
    private Trigger trigger;
    private VCS vcs;
}
