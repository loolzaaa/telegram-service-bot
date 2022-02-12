package ru.loolzaaa.telegram.servicebot.core.circleci;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import ru.loolzaaa.telegram.servicebot.core.circleci.pojo.*;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CircleCIRequest {
    private String id;
    private String type;
    @JsonProperty("happened_at")
    private String happenedAt;
    private Webhook webhook;
    private Project project;
    private Organization organization;
    private Workflow workflow;
    private Pipeline pipeline;
    private Job job;
}
