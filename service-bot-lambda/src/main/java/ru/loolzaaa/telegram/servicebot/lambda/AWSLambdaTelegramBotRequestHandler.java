package ru.loolzaaa.telegram.servicebot.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;
import ru.loolzaaa.telegram.servicebot.impl.circleci.CircleCILongWebhookBot;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.BotUser;
import ru.loolzaaa.telegram.servicebot.lambda.request.RequestDispatcher;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Map;

public class AWSLambdaTelegramBotRequestHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private static final S3Client S3;

	private static final RequestDispatcher requestDispatcher;

	private static final ObjectMapper objectMapper;

	private static final BotConfiguration<BotUser> configuration;

	static {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		S3 = S3Client.builder().region(Region.US_EAST_2).build();

		configuration = loadConfigurationFromS3();

		CircleCILongWebhookBot bot = new CircleCILongWebhookBot(configuration, null, null, null);
		requestDispatcher = new RequestDispatcher(bot);
		requestDispatcher.setObjectMapper(objectMapper);
	}

	@Override
	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent apiGatewayV2HTTPEvent, Context context) {
		final String routeKey = apiGatewayV2HTTPEvent.getRouteKey().substring(apiGatewayV2HTTPEvent.getRouteKey().indexOf("/"));
		context.getLogger().log(String.format("Request from: %s -> %s", apiGatewayV2HTTPEvent.getRouteKey(), routeKey));

		Map<String, String> headers = Map.of("Content-Type", "application/json;charset=UTF-8");
		APIGatewayV2HTTPResponse apiResponse = APIGatewayV2HTTPResponse.builder()
				.withIsBase64Encoded(false)
				.withHeaders(headers)
				.withStatusCode(200)
				.build();
		try {
			apiResponse.setBody(requestDispatcher.dispatch(routeKey, apiGatewayV2HTTPEvent, context));
			return apiResponse;
		} catch (JsonProcessingException | TelegramApiValidationException e) {
			context.getLogger().log("Incorrect request: " + e.getMessage());
			apiResponse.setStatusCode(400);
			return apiResponse;
		} catch (Exception e) {
			e.printStackTrace();
			apiResponse.setStatusCode(500);
			return apiResponse;
		} finally {
			saveConfigurationToS3();
		}
	}

	private static BotConfiguration<BotUser> loadConfigurationFromS3() {
		GetObjectRequest request = GetObjectRequest.builder()
				.bucket(System.getenv("s3_config_bucket"))
				.key(System.getenv("s3_config_key"))
				.build();
		try {
			return objectMapper.readValue(S3.getObject(request), new TypeReference<>(){});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void saveConfigurationToS3() {
		PutObjectRequest request = PutObjectRequest.builder()
				.bucket(System.getenv("s3_config_bucket"))
				.key(System.getenv("s3_config_key"))
				.build();
		try {
			S3.putObject(request, RequestBody.fromString(objectMapper.writeValueAsString(configuration)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
