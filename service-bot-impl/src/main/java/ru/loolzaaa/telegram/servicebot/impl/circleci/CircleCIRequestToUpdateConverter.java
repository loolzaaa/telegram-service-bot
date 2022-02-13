package ru.loolzaaa.telegram.servicebot.impl.circleci;

import org.telegram.telegrambots.meta.api.objects.*;

import java.util.List;
import java.util.Random;
import java.util.StringJoiner;

public class CircleCIRequestToUpdateConverter {
    public static Update convert(CircleCIRequest request) {
        Update update = new Update();

        User user = new User();
        user.setId(1000000000L);
        user.setIsBot(true);
        user.setFirstName("CircleCI");

        Chat chat = new Chat();
        chat.setId(1000000000L);
        chat.setType("private");

        String text = getMessageText(request);

        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setType("bot_command");
        messageEntity.setOffset(0);
        messageEntity.setLength(text.length());

        Message message = new Message();
        message.setMessageId(new Random().nextInt(Integer.MAX_VALUE));
        message.setFrom(user);
        message.setDate((int)(System.currentTimeMillis() / 1000L));
        message.setChat(chat);
        message.setText(text);
        message.setEntities(List.of(messageEntity));

        update.setUpdateId(1000000000);
        update.setMessage(message);
        return update;
    }

    private static String getMessageText(CircleCIRequest request) {
        return new StringJoiner(" ", "/circleci_result ", "")
                .add(request.getType() != null ? request.getType() : "EMPTY_TYPE")
                .add(request.getHappenedAt() != null ? request.getHappenedAt() : "EMPTY_TIME")
                .add(request.getProject() != null ? request.getProject().getName() : "EMPTY_PROJECT_NAME")
                .add(request.getProject() != null ? request.getProject().getSlug() : "EMPTY_PROJECT_SLUG")
                .add(request.getWorkflow() != null ? request.getWorkflow().getName() : "EMPTY_WORKFLOW_NAME")
                .add(request.getWorkflow() != null ? request.getWorkflow().getStatus() : "EMPTY_WORKFLOW_STATUS")
                .toString();
    }
}
