package ru.loolzaaa.telegram.servicebot.core.circleci.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Commit {
    private String subject;
    private String body;
    private Author author;
    @JsonProperty("authored_at")
    private String authoredAt;
    private Committer committer;
    @JsonProperty("commited_at")
    private String commitedAt;
}
