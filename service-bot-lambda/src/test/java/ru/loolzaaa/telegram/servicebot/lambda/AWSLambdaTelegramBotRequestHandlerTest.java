package ru.loolzaaa.telegram.servicebot.lambda;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.BotUser;
import ru.loolzaaa.telegram.servicebot.lambda.config.GlobalConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class AWSLambdaTelegramBotRequestHandlerTest {

    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldCorrectDeserializeGlobalConfiguration() throws Exception {
        final String JSONConfig = "{\"circleci\":{\"users\":[], \"fakeProperty\":{}},\"fakeService\":{}}";

        GlobalConfiguration globalConfiguration = objectMapper.readValue(JSONConfig, GlobalConfiguration.class);

        assertThat(globalConfiguration).isNotNull();
        assertThat(globalConfiguration.getCircleCIBotConfiguration()).isNotNull();
        assertThat(globalConfiguration.getCircleCIBotConfiguration().getUsers()).isNotNull();
    }
}