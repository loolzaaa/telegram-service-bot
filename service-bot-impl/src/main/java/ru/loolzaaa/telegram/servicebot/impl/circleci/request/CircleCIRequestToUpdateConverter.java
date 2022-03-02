package ru.loolzaaa.telegram.servicebot.impl.circleci.request;

import org.telegram.telegrambots.meta.api.objects.*;

import java.util.List;
import java.util.Random;
import java.util.StringJoiner;

public class CircleCIRequestToUpdateConverter {

    private static final String CIRCLECI_RESULTKEY = System.getenv("circleci_resultKey");

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
                .add(CIRCLECI_RESULTKEY != null ? CIRCLECI_RESULTKEY : "KEY")
                .add(request.getType() != null ? request.getType() : "EMPTY_TYPE")
                .add(request.getWorkflow() != null ? request.getWorkflow().getStatus() : "EMPTY_WORKFLOW_STATUS")
                .add(request.getPipeline().getVcs().getCommit().getAuthor().getName() != null ? request.getPipeline().getVcs().getCommit().getAuthor().getName() : "EMPTY_COMMIT_AUTHOR")
                .add(request.getProject() != null ? request.getProject().getSlug() : "EMPTY_PROJECT_SLUG")
                .add(request.getWorkflow() != null ? request.getWorkflow().getName() : "EMPTY_WORKFLOW_NAME")
                .add(request.getPipeline().getVcs().getBranch() != null ? request.getPipeline().getVcs().getBranch() : "EMPTY_BRANCH_NAME")
                .add(request.getPipeline().getVcs().getCommit().getSubject() != null ? request.getPipeline().getVcs().getCommit().getSubject() : "EMPTY_COMMIT_NAME")
                .add("EMPTY_COMMIT_HASH")
                .add("EMPTY_JOB_NAME")
                .toString();
    }
}
