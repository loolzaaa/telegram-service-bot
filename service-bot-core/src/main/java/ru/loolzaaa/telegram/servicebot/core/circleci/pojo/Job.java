package ru.loolzaaa.telegram.servicebot.core.circleci.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Job {
    private String id;
    private String name;
    @JsonProperty("started_at")
    private String startedAt;
    @JsonProperty("stopped_at")
    private String stoppedAt;
    private String status;
    private int number;
}
