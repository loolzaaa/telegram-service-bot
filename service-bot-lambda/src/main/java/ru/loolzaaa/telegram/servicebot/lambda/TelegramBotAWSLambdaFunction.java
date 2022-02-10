package ru.loolzaaa.telegram.servicebot.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;
import ru.loolzaaa.telegram.servicebot.core.bot.ServiceWebhookBot;
import ru.loolzaaa.telegram.servicebot.core.bot.pojo.Configuration;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TelegramBotAWSLambdaFunction implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private static final S3Client S3 = S3Client.builder().region(Region.US_EAST_2).build();

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private ServiceWebhookBot bot;

	static {
		MAPPER.registerModule(new JavaTimeModule());
	}

	@Override
	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent apiGatewayV2HTTPEvent, Context context) {
		Configuration configuration = loadConfigurationFromS3();

		this.bot = new ServiceWebhookBot(configuration);

		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/json;charset=UTF-8");

		int status;
		String body;
		BotApiMethod<?> method;
		try {
			Update update = MAPPER.readValue(apiGatewayV2HTTPEvent.getBody(), Update.class);
			method = this.bot.onWebhookUpdateReceived(update);
			if (method != null) {
				method.validate();
				status = 200;
				body = MAPPER.writeValueAsString(method);
			} else {
				status = 200;
				body = "{}";
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (TelegramApiValidationException e) {
			status = 500;
			body = "{}";
		}

		saveConfigurationToS3();

		return APIGatewayV2HTTPResponse.builder()
				.withStatusCode(status)
				.withIsBase64Encoded(false)
				.withHeaders(headers)
				.withBody(body)
				.build();
	}

	private Configuration loadConfigurationFromS3() {
		GetObjectRequest request = GetObjectRequest.builder()
				.bucket(System.getenv("s3_config_bucket"))
				.key(System.getenv("s3_config_key"))
				.build();
		try {
			return MAPPER.readValue(S3.getObject(request), Configuration.class);
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
			S3.putObject(request, RequestBody.fromString(MAPPER.writeValueAsString(this.bot.getConfiguration())));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
