package ru.loolzaaa.telegram.servicebot.core.circleci.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VCS {
    @JsonProperty("provider_name")
    private String providerName;
    @JsonProperty("origin_repository_url")
    private String originRepositoryUrl;
    @JsonProperty("target_repository_url")
    private String targetRepositoryUrl;
    private String revision;
    private Commit commit;
    private String branch;
}
