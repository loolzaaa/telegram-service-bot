package ru.loolzaaa.telegram.servicebot.lambda.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.CircleCIBotConfiguration;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GlobalConfiguration {
    @JsonProperty("circleci")
    private CircleCIBotConfiguration circleCIBotConfiguration;
}
