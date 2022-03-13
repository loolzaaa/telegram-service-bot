package ru.loolzaaa.telegram.servicebot.impl.circleci.request;

import org.telegram.telegrambots.meta.api.objects.*;
import ru.loolzaaa.telegram.servicebot.impl.circleci.request.pojo.Commit;
import ru.loolzaaa.telegram.servicebot.impl.circleci.request.pojo.VCS;

import java.util.List;
import java.util.Random;
import java.util.StringJoiner;

public class CircleCIRequestToUpdateConverter {

    private static final String COMMIT_NAME_START = "<<<$";
    private static final String COMMIT_NAME_END = "$>>>";

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
        VCS vcs = request.getPipeline() == null ? null : request.getPipeline().getVcs();
        Commit commit = vcs == null ? null : request.getPipeline().getVcs().getCommit();

        String type = request.getType() == null ?
                "EMPTY_TYPE" :
                !request.getType().contains(" ") ?
                        request.getType() :
                        request.getType().substring(0, request.getType().indexOf(" "));

        String workflowStatus = ((request.getWorkflow() == null) || (request.getWorkflow().getStatus() == null)) ?
                "EMPTY_WORKFLOW_STATUS" :
                !request.getWorkflow().getStatus().contains(" ") ?
                        request.getWorkflow().getStatus() :
                        request.getWorkflow().getStatus().substring(0, request.getWorkflow().getStatus().indexOf(" "));

        String commitAuthor = ((commit == null) || (commit.getAuthor() == null) || (commit.getAuthor().getName() == null)) ?
                "EMPTY_COMMIT_AUTHOR" :
                !commit.getAuthor().getName().contains(" ") ?
                        commit.getAuthor().getName() :
                        commit.getAuthor().getName().substring(0, commit.getAuthor().getName().indexOf(" "));

        String projectSlug = ((request.getProject() == null) || (request.getProject().getSlug() == null)) ?
                "EMPTY_PROJECT_SLUG" :
                !request.getProject().getSlug().contains(" ") ?
                        request.getProject().getSlug() :
                        request.getProject().getSlug().substring(0, request.getProject().getSlug().indexOf(" "));

        String workflowName = ((request.getWorkflow() == null) || (request.getWorkflow().getName() == null)) ?
                "EMPTY_WORKFLOW_NAME" :
                !request.getWorkflow().getName().contains(" ") ?
                        request.getWorkflow().getName() :
                        request.getWorkflow().getName().substring(0, request.getWorkflow().getName().indexOf(" "));

        String branchName = ((vcs == null) || (vcs.getBranch() == null)) ?
                "EMPTY_BRANCH_NAME" :
                !vcs.getBranch().contains(" ") ?
                        vcs.getBranch() :
                        vcs.getBranch().substring(0, vcs.getBranch().indexOf(" "));

        String commitSubject = ((commit == null) || (commit.getSubject() == null)) ?
                "EMPTY_COMMIT_NAME" :
                commit.getSubject();
        
        return new StringJoiner(" ", "/circleci_result ", "")
                .add(CIRCLECI_RESULTKEY != null ? CIRCLECI_RESULTKEY : "KEY")
                .add(type)
                .add(workflowStatus)
                .add(commitAuthor)
                .add(projectSlug)
                .add(workflowName)
                .add(branchName)
                .add(COMMIT_NAME_START)
                .add(commitSubject)
                .add(COMMIT_NAME_END)
                .add("EMPTY_COMMIT_HASH")
                .add("EMPTY_JOB_NAME")
                .toString();
    }
}
