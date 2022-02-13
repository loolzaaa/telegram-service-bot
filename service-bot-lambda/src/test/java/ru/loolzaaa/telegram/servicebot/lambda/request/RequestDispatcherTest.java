package ru.loolzaaa.telegram.servicebot.lambda.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.loolzaaa.telegram.servicebot.core.bot.ServiceWebhookBot;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RequestDispatcherTest {

    @Mock
    private ServiceWebhookBot serviceWebhookBot;

    @InjectMocks
    private RequestDispatcher requestDispatcher;

    @BeforeEach
    void setUp() {
        System.out.println(requestDispatcher);
        System.out.println(serviceWebhookBot);
    }

    @Test
    void test() throws Exception {
        final String KEY = "secret";
        final String BODY = "hello world";
        final String expectedSign = "734cc62f32841568f45715aeb9f4d7891324e6d948e4c6c60c0621cdac48623a";

        SecretKeySpec secretKeySpec = new SecretKeySpec(KEY.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);
        String actualSign = bytesToHex(mac.doFinal(BODY.getBytes()));

        assertThat(actualSign).isEqualToIgnoringCase(expectedSign);
    }

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
    private static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }
}