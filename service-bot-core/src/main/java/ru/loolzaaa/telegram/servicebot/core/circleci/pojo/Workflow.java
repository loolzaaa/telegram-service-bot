package ru.loolzaaa.telegram.servicebot.core.circleci.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Workflow {
    private String id;
    private String name;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("stopped_at")
    private String stoppedAt;
    private String url;
    private String status;
}
